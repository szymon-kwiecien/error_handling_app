document.addEventListener('DOMContentLoaded', () => {
    const chatContainer = document.getElementById('chat-container');
    const chatMessages = document.getElementById('chat-messages');
    const chatForm = document.getElementById('chat-form');
    const chatInput = document.getElementById('chat-input');
    const reportId = chatContainer.dataset.reportId;
    const currentUsername = chatContainer.dataset.username;

    let stompClient;
    let currentPage = 0;
    let loading = false;
    let allMessagesLoaded = false;
    let webSocketConnected = false;
    let connecting = false;
    let disableScrollLoading = false;

    function connectWebSocket() {
        if (webSocketConnected || connecting) return;

        connecting = true;
        const socket = new SockJS('/ws-chat');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, () => {
            console.log('Połaczono z Websocket');
            stompClient.subscribe('/topic/chat.' + reportId, (message) => {
                const msg = JSON.parse(message.body);
                if (msg.reportId.toString() !== reportId.toString()) return;

                disableScrollLoading = false;

                appendMessage(msg, true);
            });

            stompClient.subscribe('/user/queue/errors', (message) => {
                const error = JSON.parse(message.body);
                alert("Błąd: " + error.errorMessage);
            });

            webSocketConnected = true;
            connecting = false;
        }, (error) => {
            console.error('WebSocket error  :', error);
            connecting = false;
        });
    }

    function createMessageElement(msg) {
        const div = document.createElement('div');
        const isMe = msg.sender === currentUsername;
        const borderColor = isMe ? 'border-yellow-500' : 'border-indigo-500';
        const bgColor = isMe ? 'bg-gray-800/80' : 'bg-gray-800/40';
        div.className = `chat-message mb-4 p-3 rounded border-l-4 ${borderColor} ${bgColor}`;

        let date;
        if (Array.isArray(msg.timestamp)) {
            date = new Date(msg.timestamp[0], msg.timestamp[1] - 1, msg.timestamp[2], msg.timestamp[3], msg.timestamp[4]);
        } else {
            date = new Date(msg.timestamp);
        }

        const dateTimeStr = date.toLocaleString('pl-PL', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });

        const header = document.createElement('div');
        header.className = 'flex justify-between items-center mb-2';

        const nameNode = document.createElement('strong');
        nameNode.className = isMe ? 'text-yellow-400 text-sm' : 'text-indigo-300 text-sm';
        nameNode.textContent = isMe ? 'Ty' : msg.sender;

        const timeNode = document.createElement('small');
        timeNode.className = 'text-gray-500 text-xs';
        timeNode.textContent = dateTimeStr;

        header.appendChild(nameNode);
        header.appendChild(timeNode);

        const content = document.createElement('div');
        content.className = 'text-base text-gray-100 break-words leading-relaxed font-medium';
        content.textContent = msg.content;

        div.appendChild(header);
        div.appendChild(content);
        return div;
    }

    function appendMessage(msg, scrollToBottom = false) {
        const div = createMessageElement(msg); //wiadomosc z webSocket
        chatMessages.appendChild(div);
        if (scrollToBottom) {
            chatContainer.scrollTop = chatContainer.scrollHeight;
        }
    }

    function prependMessage(msg) {
        const div = createMessageElement(msg); //wiadmosc z bazy danych
        chatMessages.insertBefore(div, chatMessages.firstChild);
    }

    async function loadMessages() {
        if (loading || allMessagesLoaded || disableScrollLoading) return;
        loading = true;

        try {
            const prevScrollHeight = chatContainer.scrollHeight;

            const response = await fetch(`/api/chat/history/${reportId}?page=${currentPage}&size=15`);
            const data = await response.json();
            const messages = data.content;

            if (!messages || messages.length === 0) {
                allMessagesLoaded = true;
            } else {
                messages.forEach(prependMessage);
                currentPage++;

                const newScrollHeight = chatContainer.scrollHeight;
                chatContainer.scrollTop = newScrollHeight - prevScrollHeight;
            }
        } catch (e) {
            console.error("Błąd przy ładowaniu wiadomości:", e);
        }

        loading = false;
    }

    if (chatForm) {
        chatForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const content = chatInput.value.trim();
            if (!content) return;

            const message = {
                reportId: reportId,
                content: content,
                sender: currentUsername
            };

            stompClient.send("/app/chat", {}, JSON.stringify(message));
            chatInput.value = '';

            disableScrollLoading = true;
        });
    }

    chatContainer.addEventListener('scroll', () => {
        if (chatContainer.scrollTop === 0 && !loading) {
            loadMessages();
        }
    });

    connectWebSocket();
    loadMessages().then(() => {
        requestAnimationFrame(() => {
            chatContainer.scrollTop = chatContainer.scrollHeight;
        });
    });
});
