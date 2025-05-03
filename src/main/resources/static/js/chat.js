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

            webSocketConnected = true;
            connecting = false;
        }, (error) => {
            console.error('WebSocket error  :', error);
            connecting = false;
        });
    }

    function createMessageElement(msg, fromWebSocket = false) {
        const div = document.createElement('div');
        const date = new Date(msg.timestamp);

        if (fromWebSocket) {
            date.setHours(date.getHours() + 2);
        }

        const time = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        div.innerHTML = `<strong>${msg.sender}</strong> <small>${time}</small><br>${msg.content}`;
        return div;
    }

    function appendMessage(msg, scrollToBottom = false) {
        const div = createMessageElement(msg, true); //wiadomosc z webSocket
        chatMessages.appendChild(div);
        if (scrollToBottom) {
            chatContainer.scrollTop = chatContainer.scrollHeight;
        }
    }

    function prependMessage(msg) {
        const div = createMessageElement(msg, false); //wiadmosc z bazy danych
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
                sender: currentUsername,
                timestamp: new Date().toISOString()
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
        chatContainer.scrollTop = chatContainer.scrollHeight;
    });
});
