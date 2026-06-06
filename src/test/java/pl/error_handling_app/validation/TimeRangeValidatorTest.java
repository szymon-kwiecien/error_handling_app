package pl.error_handling_app.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.error_handling_app.company.dto.CompanyDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeRangeValidatorTest {

    private final TimeRangeValidator validator = new TimeRangeValidator();

    @Mock
    private ConstraintValidatorContext context;

    @Test
    void shouldReturnTrueWhenCompanyIsNull() {
        //when
        boolean isValid = validator.isValid(null, context);

        //then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnTrueWhenTimeToResolveIsGreaterThanTimeToFirstRespond() {
        //given
        CompanyDto company = mock(CompanyDto.class);
        when(company.timeToFirstRespond()).thenReturn(24);
        when(company.timeToResolve()).thenReturn(48);

        //when
        boolean isValid = validator.isValid(company, context);

        //then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTimeToResolveIsLessThanTimeToFirstRespond() {
        //given
        CompanyDto company = mock(CompanyDto.class);
        when(company.timeToFirstRespond()).thenReturn(48);
        when(company.timeToResolve()).thenReturn(24);

        //when
        boolean isValid = validator.isValid(company, context);

        //then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTimesAreEqual() {
        //given
        CompanyDto company = mock(CompanyDto.class);
        when(company.timeToFirstRespond()).thenReturn(24);
        when(company.timeToResolve()).thenReturn(24);

        //when
        boolean isValid = validator.isValid(company, context);

        //then
        assertThat(isValid).isFalse();
    }
}