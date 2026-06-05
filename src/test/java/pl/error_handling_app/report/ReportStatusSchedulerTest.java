package pl.error_handling_app.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.error_handling_app.report.entity.Report;
import pl.error_handling_app.report.repository.ReportRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportStatusSchedulerTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportStatusScheduler reportStatusScheduler;

    @Captor
    private ArgumentCaptor<List<Report>> reportsCaptor;

    @Test
    void shouldUpdatePendingAndUnderReviewReportsToOverdue() {
        //given
        Report pendingReport = new Report();
        pendingReport.setStatus(ReportStatus.PENDING);

        Report underReviewReport = new Report();
        underReviewReport.setStatus(ReportStatus.UNDER_REVIEW);

        //symulacja ze repozytorium zawiera po jednym przeterminowanym zgłoszeniu każdego typu
        when(reportRepository.findByStatusAndTimeToRespondLessThanEqual(eq(ReportStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(pendingReport));

        when(reportRepository.findByStatusAndDueDateLessThanEqual(eq(ReportStatus.UNDER_REVIEW), any(LocalDateTime.class)))
                .thenReturn(List.of(underReviewReport));

        //when
        reportStatusScheduler.updateOverdueReports();

        //then
        verify(reportRepository, times(1)).saveAll(reportsCaptor.capture());

        List<Report> savedReports = reportsCaptor.getValue();

        assertThat(savedReports).hasSize(2);
        assertThat(savedReports).containsExactlyInAnyOrder(pendingReport, underReviewReport);
        //sprawdzenie czy statusy zostały zmienione na OVERDUE
        assertThat(pendingReport.getStatus()).isEqualTo(ReportStatus.OVERDUE);
        assertThat(underReviewReport.getStatus()).isEqualTo(ReportStatus.OVERDUE);
    }

    @Test
    void shouldDoNothingWhenNoOverdueReportsExist() {
        //given
        when(reportRepository.findByStatusAndTimeToRespondLessThanEqual(eq(ReportStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        when(reportRepository.findByStatusAndDueDateLessThanEqual(eq(ReportStatus.UNDER_REVIEW), any(LocalDateTime.class)))
                .thenReturn(List.of());

        //when
        reportStatusScheduler.updateOverdueReports();

        //then
        verify(reportRepository, times(1)).saveAll(reportsCaptor.capture());
        List<Report> savedReports = reportsCaptor.getValue();

        assertThat(savedReports).isEmpty();
    }
}