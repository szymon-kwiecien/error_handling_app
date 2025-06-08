package pl.error_handling_app.company;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TimeRangeValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimeRange {
    String message() default "Czas na rozwiązanie musi być dłuższy niż czas na pierwszą reakcję";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
