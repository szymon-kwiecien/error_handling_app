package pl.error_handling_app.company;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.error_handling_app.company.CompanyDto;

public class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, CompanyDto> {

    @Override
    public boolean isValid(CompanyDto company, ConstraintValidatorContext context) {
        if (company == null) {
            return true;
        }
        return company.getTimeToResolve() > company.getTimeToFirstRespond();
    }
}
