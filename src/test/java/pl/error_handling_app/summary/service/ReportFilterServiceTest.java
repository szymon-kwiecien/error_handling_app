package pl.error_handling_app.summary.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.report.dto.ReportDtoMapper;
import pl.error_handling_app.report.entity.Report;
import pl.error_handling_app.report.entity.ReportCategory;
import pl.error_handling_app.report.repository.ReportRepository;
import pl.error_handling_app.summary.dto.SummaryFormRequest;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportFilterServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportDtoMapper reportMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportFilterService reportFilterService;

    @Test
    void shouldFilterReportsWithOnlyDatesWhenOtherFiltersAreNull() {
        //given
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 5, 31);

        SummaryFormRequest request = new SummaryFormRequest(
                dateFrom, dateTo, null, null, null, "id,asc", false, false
        );

        Report dummyReport = new Report();
        ReportDto dummyDto = mock(ReportDto.class);

        when(reportRepository.findAll(any(Specification.class))).thenReturn(List.of(dummyReport));
        when(reportMapper.mapToDto(dummyReport)).thenReturn(dummyDto);

        when(userService.findUserByEmail("all")).thenReturn(Optional.empty());

        //when
        List<ReportDto> result = reportFilterService.prepareFilteredReports(request, null, dateFrom, dateTo);

        //then
        assertThat(result).hasSize(1).containsExactly(dummyDto);

        verify(userService, times(1)).findUserByEmail("all");
    }

    @Test
    void shouldFilterReportsWithAllAvailableFilters() {
        //given
        LocalDate dateFrom = LocalDate.of(2026, 6, 1);
        LocalDate dateTo = LocalDate.of(2026, 6, 30);
        String employeeEmail = "test@test.pl";

        SummaryFormRequest request = new SummaryFormRequest(
                dateFrom, dateTo, "Błędy IT", ReportStatus.UNDER_REVIEW, employeeEmail, "id,asc", true, true
        );

        ReportCategory dummyCategory = new ReportCategory();
        dummyCategory.setName("Błędy IT");

        User dummyUser = new User();
        dummyUser.setEmail(employeeEmail);

        Report dummyReport = new Report();
        ReportDto dummyDto = mock(ReportDto.class);

        when(userService.findUserByEmail(employeeEmail)).thenReturn(Optional.of(dummyUser));
        when(reportRepository.findAll(any(Specification.class))).thenReturn(List.of(dummyReport));
        when(reportMapper.mapToDto(dummyReport)).thenReturn(dummyDto);

        //when
        List<ReportDto> result = reportFilterService.prepareFilteredReports(request, dummyCategory, dateFrom, dateTo);

        //then
        assertThat(result).hasSize(1).containsExactly(dummyDto);

        verify(userService, times(1)).findUserByEmail(employeeEmail);
        verify(reportRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void shouldNotAddUserSpecificationIfUserIsNotFoundInDatabase() {
        //given
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        String nonExistentUserEmail = "test@test.pl";

        SummaryFormRequest request = new SummaryFormRequest(
                dateFrom, dateTo, null, null, nonExistentUserEmail, "id,asc", false, false
        );

        when(userService.findUserByEmail(nonExistentUserEmail)).thenReturn(Optional.empty());
        when(reportRepository.findAll(any(Specification.class))).thenReturn(List.of());

        //when
        List<ReportDto> result = reportFilterService.prepareFilteredReports(request, null, dateFrom, dateTo);

        //then
        assertThat(result).isEmpty();

        verify(userService).findUserByEmail(nonExistentUserEmail);
        verify(reportRepository).findAll(any(Specification.class));
    }
}