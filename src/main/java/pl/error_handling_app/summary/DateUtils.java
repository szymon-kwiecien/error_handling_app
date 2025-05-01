package pl.error_handling_app.summary;

import java.time.LocalDate;

public class DateUtils {

    static LocalDate[] checkAnyDateIsNull(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null) {
            dateFrom = LocalDate.of(1970, 1,1);
        }
        if (dateTo == null) {
            dateTo = LocalDate.now();
        }
        return new LocalDate[] {dateFrom, dateTo};
    }
}
