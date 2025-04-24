package pl.error_handling_app.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.report.id = :reportId")
    Page<ChatMessage> findByReportId(Long reportId, Pageable pageable);
    @Query("SELECT MAX(m.timestamp) FROM ChatMessage m WHERE m.report.id = :reportId")
    LocalDateTime findLastMessageTimeByReportId(Long reportId);

}
