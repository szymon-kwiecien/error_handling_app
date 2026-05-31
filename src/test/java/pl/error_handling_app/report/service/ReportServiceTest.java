package pl.error_handling_app.report.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import pl.error_handling_app.attachment.entity.Attachment;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.exception.*;
import pl.error_handling_app.file.FileService;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.dto.NewReportDto;
import pl.error_handling_app.report.dto.ReportDetailsDto;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.report.dto.ReportDtoMapper;
import pl.error_handling_app.report.entity.Report;
import pl.error_handling_app.report.entity.ReportCategory;
import pl.error_handling_app.report.repository.ReportRepository;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.entity.UserRole;
import pl.error_handling_app.user.repository.UserRepository;
import pl.error_handling_app.utils.SecurityUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;
    @Mock private ReportCategoryService reportCategoryService;
    @Mock private UserRepository userRepository;
    @Mock private FileService fileService;
    @Mock private ReportDtoMapper reportMapper;
    @Mock private ReportStatisticsService statisticsService;

    @InjectMocks
    private ReportService reportService;

    private User mockUser;
    private UserRole roleAdmin;
    private UserRole roleEmployee;
    private UserRole roleUser;
    private Report testReport;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        testReport = new Report();
        testReport.setId(1L);
        testReport.setDatedAdded(LocalDateTime.now().minusDays(1));

        roleAdmin = new UserRole();
        roleAdmin.setName(SecurityUtils.ADMIN_ROLE);

        roleEmployee = new UserRole();
        roleEmployee.setName(SecurityUtils.EMPLOYEE_ROLE);

        roleUser = new UserRole();
        roleUser.setName("USER");
    }

    @Test
    void shouldFindReportsForAdminWithoutExtraFilters() {
        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getCurrentUserEmail).thenReturn("admin@test.pl");
            when(userRepository.findByEmail("admin@test.pl")).thenReturn(Optional.of(mockUser));
            when(mockUser.getRoles()).thenReturn(Set.of(roleAdmin));

            Page<Report> mockPage = new PageImpl<>(List.of(testReport));
            when(reportRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

            //when
            Page<ReportDto> result = reportService.findReports("Tytuł", ReportStatus.PENDING, Pageable.unpaged());

            //then
            verify(reportRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            assertThat(result).isNotNull();
        }
    }

    @Test
    void shouldAuthorizeUserWhoIsOwner() {
        testReport.setReportingUser(mockUser);
        when(mockUser.getRoles()).thenReturn(Set.of(roleUser));

        boolean isAuthorized = reportService.isUserAuthorizedForReport(testReport, mockUser);
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldAuthorizeUserWhoIsAssignedEmployee() {
        testReport.setReportingUser(new User());
        testReport.setAssignedEmployee(mockUser);
        when(mockUser.getRoles()).thenReturn(Set.of(roleEmployee));

        boolean isAuthorized = reportService.isUserAuthorizedForReport(testReport, mockUser);
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldNotAuthorizeRandomUser() {
        User randomUser = mock(User.class);
        when(randomUser.getRoles()).thenReturn(Set.of(roleUser));
        testReport.setReportingUser(mockUser);

        boolean isAuthorized = reportService.isUserAuthorizedForReport(testReport, randomUser);
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldAddNewReportSuccessfullyWithFiles() {
        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getCurrentUserEmail).thenReturn("test@fixaro.pl");

            NewReportDto newReportDto = mock(NewReportDto.class);
            when(newReportDto.getCategoryId()).thenReturn(1L);
            when(newReportDto.getTitle()).thenReturn("Błąd");
            List<MultipartFile> files = List.of(mock(MultipartFile.class));
            when(newReportDto.getFile()).thenReturn(files);

            ReportCategory category = new ReportCategory();
            when(reportCategoryService.getCategoryById(1L)).thenReturn(Optional.of(category));
            when(userRepository.findByEmail("test@fixaro.pl")).thenReturn(Optional.of(mockUser));
            when(mockUser.getEmail()).thenReturn("test@fixaro.pl");

            Company company = mock(Company.class);
            when(mockUser.getCompany()).thenReturn(company);
            when(company.getTimeToFirstRespond()).thenReturn(24);
            when(company.getTimeToResolve()).thenReturn(48);

            when(fileService.areFilesValid(files)).thenReturn(true);
            when(fileService.storeFiles(files, "test@fixaro.pl")).thenReturn(List.of(new Attachment()));

            //when
            reportService.addNewReport(newReportDto);

            //then
            verify(reportRepository, times(1)).save(any(Report.class));
            verify(fileService, times(1)).storeFiles(files, "test@fixaro.pl");
        }
    }

    @Test
    void shouldCloseReportSuccessfully() {
        testReport.setStatus(ReportStatus.PENDING);
        testReport.setReportingUser(mockUser);
        when(mockUser.getRoles()).thenReturn(Set.of(roleAdmin));
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(userRepository.findByEmail("test@fixaro.pl")).thenReturn(Optional.of(mockUser));

        //when
        reportService.closeReport(1L, "test@fixaro.pl");

        //then
        assertThat(testReport.getStatus()).isEqualTo(ReportStatus.COMPLETED);
    }

    @Test
    void shouldThrowExceptionWhenClosingAlreadyCompletedReport() {
        testReport.setStatus(ReportStatus.COMPLETED);
        testReport.setReportingUser(mockUser);
        when(mockUser.getRoles()).thenReturn(Set.of(roleAdmin));
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(userRepository.findByEmail("test@fixaro.pl")).thenReturn(Optional.of(mockUser));

        assertThrows(ReportAlreadyCompletedException.class, () -> reportService.closeReport(1L, "test@fixaro.pl"));
    }

    @Test
    void shouldDeleteReportSuccessfully() {
        testReport.setReportingUser(mockUser);
        when(mockUser.getRoles()).thenReturn(Set.of(roleAdmin));
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(userRepository.findByEmail("test@fixaro.pl")).thenReturn(Optional.of(mockUser));

        //when
        reportService.deleteReport(1L, "test@fixaro.pl");

        //then
        verify(reportRepository, times(1)).delete(testReport);
    }

    @Test
    void shouldAssignEmployeeSuccessfully() {
        User employee = mock(User.class);
        when(employee.getRoles()).thenReturn(Set.of(roleEmployee));
        testReport.setStatus(ReportStatus.PENDING);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));

        //when
        reportService.assignEmployeeToReport(1L, 2L);

        //then
        assertThat(testReport.getAssignedEmployee()).isEqualTo(employee);
        assertThat(testReport.getStatus()).isEqualTo(ReportStatus.UNDER_REVIEW);
    }

    @Test
    void shouldThrowExceptionWhenAssigningUserWithoutEmployeeRole() {
        User nonEmployee = mock(User.class);
        when(nonEmployee.getRoles()).thenReturn(Set.of(roleUser));
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(userRepository.findById(2L)).thenReturn(Optional.of(nonEmployee));

        assertThrows(UserLacksRequiredRoleException.class, () -> reportService.assignEmployeeToReport(1L, 2L));
    }

    @Test
    void shouldThrowExceptionWhenFilesAreInvalid() {
        List<MultipartFile> files = List.of(mock(MultipartFile.class));
        when(fileService.areFilesValid(files)).thenReturn(false);

        assertThrows(InvalidAttachmentException.class, () -> reportService.addAttachmentsToExistingReport(1L, files));
    }

    @Test
    void shouldReturnZeroPercentageIfReportIsCompletedOrOverdue() {
        ReportDto overdueReport = mock(ReportDto.class);
        when(overdueReport.getStatusName()).thenReturn(ReportStatus.OVERDUE.polishName);
        ReportDto completedReport = mock(ReportDto.class);
        when(completedReport.getStatusName()).thenReturn(ReportStatus.COMPLETED.polishName);

        assertThat(reportService.calculateTimeLeftPercentage(overdueReport)).isZero();
        assertThat(reportService.calculateTimeLeftPercentage(completedReport)).isZero();
    }

    @Test
    void shouldSortReportsByAddedDateAscending() {
        ReportDto r1 = mock(ReportDto.class);
        when(r1.getDateAdded()).thenReturn(LocalDateTime.of(2026, 1, 10, 10, 0));
        ReportDto r2 = mock(ReportDto.class);
        when(r2.getDateAdded()).thenReturn(LocalDateTime.of(2026, 1, 1, 10, 0));

        List<ReportDto> reports = new ArrayList<>(List.of(r1, r2));

        //when
        String sortLabel = reportService.sortReports(reports, "addedDateAsc");

        //then
        assertThat(reports.get(0)).isEqualTo(r2);
        assertThat(sortLabel).isEqualTo("Data zgłoszenia (od najstarszych)");
    }

    @Test
    void shouldGetReportForChatSuccessfully() {
        testReport.setReportingUser(mockUser);
        when(mockUser.getRoles()).thenReturn(Set.of(roleAdmin));
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(userRepository.findByEmail("test@fixaro.pl")).thenReturn(Optional.of(mockUser));

        ReportDetailsDto detailsDto = mock(ReportDetailsDto.class);
        when(reportMapper.mapToReportDetailsDto(testReport)).thenReturn(detailsDto);

        //when
        ReportDetailsDto result = reportService.getReportForChat(1L, "test@fixaro.pl");

        //then
        assertThat(result).isEqualTo(detailsDto);
    }

    @Test
    void shouldDelegateToRepositoryWhenHandlingCompanyRemoval() {
        reportService.handleCompanyRemoval(1L);
        verify(reportRepository, times(1)).nullifyAssignmentsForCompanyUsers(1L);
        verify(reportRepository, times(1)).deleteReportsCreatedByCompanyUsers(1L);
    }

    @Test
    void shouldHandleUserRemovalSuccessfully() {
        //given
        Report assignedReport = new Report();
        assignedReport.setAssignedEmployee(mockUser);

        Report createdReport = new Report();
        createdReport.setReportingUser(mockUser);

        when(reportRepository.findAllByAssignedEmployee(mockUser)).thenReturn(List.of(assignedReport));
        when(reportRepository.findAllByReportingUser(mockUser)).thenReturn(List.of(createdReport));

        //when
        reportService.handleUserRemoval(mockUser);

        //then
        assertThat(assignedReport.getAssignedEmployee()).isNull();
        verify(reportRepository, times(1)).deleteAll(List.of(createdReport));
    }

    @Test
    void shouldAddAttachmentsToExistingReportSuccessfully() {
        //given
        List<MultipartFile> files = List.of(mock(MultipartFile.class));
        when(fileService.areFilesValid(files)).thenReturn(true);
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getCurrentUserEmail).thenReturn("test@fixaro.pl");
            when(userRepository.findByEmail("test@fixaro.pl")).thenReturn(Optional.of(mockUser));

            when(mockUser.getEmail()).thenReturn("test@fixaro.pl");

            testReport.setReportingUser(mockUser);
            when(mockUser.getRoles()).thenReturn(Set.of(roleUser));

            Attachment mockAttachment = new Attachment();
            when(fileService.storeFiles(files, "test@fixaro.pl")).thenReturn(List.of(mockAttachment));

            //when
            reportService.addAttachmentsToExistingReport(1L, files);

            //then
            assertThat(testReport.getAttachments()).hasSize(1);
            assertThat(testReport.getAttachments().get(0)).isEqualTo(mockAttachment);
        }
    }

    @Test
    void shouldCalculateRemainingTimeForReports() {
        //given
        ReportDto completedReport = mock(ReportDto.class);
        when(completedReport.getStatusName()).thenReturn(ReportStatus.COMPLETED.polishName);

        ReportDto pendingReport = mock(ReportDto.class);
        when(pendingReport.getStatusName()).thenReturn(ReportStatus.PENDING.polishName);
        when(pendingReport.getToRespondDate()).thenReturn(LocalDateTime.now().plusDays(1));

        //when
        String[] results = reportService.calculateRemainingTime(List.of(completedReport, pendingReport));

        //then
        assertThat(results).hasSize(2);
        assertThat(results[0]).isEqualTo("-");
        assertThat(results[1]).isNotEqualTo("-");
    }

    @Test
    void shouldDelegateToStatisticsServiceForAverageTimes() {
        //given
        List<ReportDto> reports = List.of(mock(ReportDto.class));
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();
        Map<LocalDate, Double> expectedMap = Map.of(start, 15.5);

        when(statisticsService.calculateAverageTimes(eq(reports), eq(start), eq(end), any()))
                .thenReturn(expectedMap);

        // when
        Map<LocalDate, Double> reactionResult = reportService.getAverageFirstReactionTimesForReports(reports, start, end);
        Map<LocalDate, Double> completionResult = reportService.getAverageCompletionTimesForReports(reports, start, end);

        //then
        assertThat(reactionResult).isEqualTo(expectedMap);
        assertThat(completionResult).isEqualTo(expectedMap);
        verify(statisticsService, times(2)).calculateAverageTimes(eq(reports), eq(start), eq(end), any());
    }
}