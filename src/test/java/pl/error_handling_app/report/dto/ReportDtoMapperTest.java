package pl.error_handling_app.report.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import pl.error_handling_app.attachment.entity.Attachment;
import pl.error_handling_app.chat.service.ChatService;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.entity.ReportCategory;
import pl.error_handling_app.report.entity.Report;
import pl.error_handling_app.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportDtoMapperTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ReportDtoMapper reportDtoMapper;

    private Report fullyPopulatedReport;
    private Report emptyReport;
    private final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        fullyPopulatedReport = new Report();
        fullyPopulatedReport.setId(10L);
        fullyPopulatedReport.setTitle("Problem z logowaniem");
        fullyPopulatedReport.setDescription("Użytkownik nie może się zalogować");
        fullyPopulatedReport.setDatedAdded(NOW);
        fullyPopulatedReport.setDueDate(NOW.plusDays(2));
        fullyPopulatedReport.setTimeToRespond(NOW.plusHours(2));
        fullyPopulatedReport.setStatus(ReportStatus.PENDING);

        ReportCategory category = mock(ReportCategory.class);
        given(category.getName()).willReturn("Software");
        fullyPopulatedReport.setCategory(category);

        Company company = mock(Company.class);
        given(company.getName()).willReturn("TechCorp");

        User reportingUser = new User();
        reportingUser.setEmail("j.kowalski@techcorp.pl");
        reportingUser.setCompany(company);
        fullyPopulatedReport.setReportingUser(reportingUser);

        User assignedUser = new User();
        assignedUser.setId(5L);
        assignedUser.setEmail("admin@fixaro.pl");
        fullyPopulatedReport.setAssignedEmployee(assignedUser);

        Attachment attachment = mock(Attachment.class);
        given(attachment.getFilePath()).willReturn("/docs/error.log");
        given(attachment.getAddingUser()).willReturn("j.kowalski@techcorp.pl");
        given(attachment.getTimestamp()).willReturn(NOW);
        given(attachment.getFileName()).willReturn("error.log");
        given(attachment.getFileSize()).willReturn("2 KB");
        given(attachment.getFileIconClass()).willReturn("fa-file");
        fullyPopulatedReport.setAttachments(List.of(attachment));

        emptyReport = new Report();
        emptyReport.setId(200L);
        emptyReport.setTitle("Nowe zgłoszenie");
        emptyReport.setAttachments(List.of());
    }

    @Test
    void shouldMapToDtoWhenAllFieldsArePresent() {
        //given
        given(chatService.getLastMessageTimeByReportId(10L)).willReturn(NOW.plusHours(1));

        //when
        ReportDto result = reportDtoMapper.mapToDto(fullyPopulatedReport);

        //then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Problem z logowaniem");
        assertThat(result.getDescription()).isEqualTo("Użytkownik nie może się zalogować");
        assertThat(result.getDateAdded()).isEqualTo(NOW);
        assertThat(result.getDueDate()).isEqualTo(NOW.plusDays(2));
        assertThat(result.getToRespondDate()).isEqualTo(NOW.plusHours(2));
        assertThat(result.getCategoryName()).isEqualTo("Software");
        assertThat(result.getStatusName()).isEqualTo(ReportStatus.PENDING.polishName);
        assertThat(result.getReportingUser()).isEqualTo("j.kowalski@techcorp.pl");
        assertThat(result.getAssignedEmployee()).isEqualTo("admin@fixaro.pl");
        assertThat(result.getLastMessageTime()).isEqualTo(NOW.plusHours(1));
        verify(chatService).getLastMessageTimeByReportId(10L);
    }

    @Test
    void shouldMapToDtoAndHandleNullRelations() {
        //given
        given(chatService.getLastMessageTimeByReportId(200L)).willReturn(null);

        //when
        ReportDto result = reportDtoMapper.mapToDto(emptyReport);

        //then
        assertThat(result.getId()).isEqualTo(200L);
        assertThat(result.getTitle()).isEqualTo("Nowe zgłoszenie");
        assertThat(result.getCategoryName()).isEqualTo("-");
        assertThat(result.getStatusName()).isEqualTo("-");
        assertThat(result.getReportingUser()).isEqualTo("-");
        assertThat(result.getAssignedEmployee()).isEqualTo("-");
        assertThat(result.getLastMessageTime()).isNull();
    }

    @Test
    void shouldMapToReportDetailsDtoWhenAllFieldsArePresent() {
        //when
        ReportDetailsDto result = reportDtoMapper.mapToReportDetailsDto(fullyPopulatedReport);

        //then
        assertThat(result.getTitle()).isEqualTo("Problem z logowaniem");
        assertThat(result.getDescription()).isEqualTo("Użytkownik nie może się zalogować");
        assertThat(result.getDateAdded()).isEqualTo(NOW);
        assertThat(result.getDueDate()).isEqualTo(NOW.plusDays(2));
        assertThat(result.getTimeToRespond()).isEqualTo(NOW.plusHours(2));
        assertThat(result.getCategoryName()).isEqualTo("Software");
        assertThat(result.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(result.getReportingUser()).isEqualTo("j.kowalski@techcorp.pl");
        assertThat(result.getReportingUserCompanyName()).isEqualTo("TechCorp");
        assertThat(result.getAssignedEmployeeId()).isEqualTo(5L);
        assertThat(result.getAssignedEmployee()).isEqualTo("admin@fixaro.pl");

        assertThat(result.getAttachments()).hasSize(1);
        assertThat(result.getAttachments().get(0).fileName()).isEqualTo("error.log");
        assertThat(result.getAttachments().get(0).fileSize()).isEqualTo("2 KB");
    }

    @Test
    void shouldMapToReportDetailsDtoAndHandleNullRelations() {
        //when
        ReportDetailsDto result = reportDtoMapper.mapToReportDetailsDto(emptyReport);

        //then
        assertThat(result.getTitle()).isEqualTo("Nowe zgłoszenie");
        assertThat(result.getCategoryName()).isEqualTo("-");
        assertThat(result.getStatus()).isNull();
        assertThat(result.getReportingUser()).isEqualTo("brak");
        assertThat(result.getReportingUserCompanyName()).isEqualTo("brak");
        assertThat(result.getAssignedEmployeeId()).isNull();
        assertThat(result.getAssignedEmployee()).isEqualTo("brak");
        assertThat(result.getAttachments()).isEmpty();
    }

    @Test
    void shouldMapToReportDetailsDtoAndHandleNullCompanyInReportingUser() {
        //given
        User reportingUserWithoutCompany = new User();
        reportingUserWithoutCompany.setEmail("bez_firmy@fixaro.pl");
        emptyReport.setReportingUser(reportingUserWithoutCompany);

        //when
        ReportDetailsDto result = reportDtoMapper.mapToReportDetailsDto(emptyReport);

        //then
        assertThat(result.getReportingUser()).isEqualTo("bez_firmy@fixaro.pl");
        assertThat(result.getReportingUserCompanyName()).isEqualTo("brak");
    }
}