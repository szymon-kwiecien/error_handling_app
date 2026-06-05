package pl.error_handling_app.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilsTest {

    @Test
    void shouldReturnSameDatesWhenNeitherIsNull() {
        //given
        LocalDate dateFrom = LocalDate.of(2020, 5, 10);
        LocalDate dateTo = LocalDate.of(2023, 11, 20);

        //when
        LocalDate[] result = DateUtils.checkAnyDateIsNull(dateFrom, dateTo);

        //then
        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo(dateFrom);
        assertThat(result[1]).isEqualTo(dateTo);
    }

    @Test
    void shouldSetDateFromTo1970WhenNull() {
        //given
        LocalDate dateTo = LocalDate.of(2023, 11, 20);

        //when
        LocalDate[] result = DateUtils.checkAnyDateIsNull(null, dateTo);

        //then
        assertThat(result[0]).isEqualTo(LocalDate.of(1970, 1, 1));
        assertThat(result[1]).isEqualTo(dateTo);
    }

    @Test
    void shouldSetDateToToTodayWhenNull() {
        //given
        LocalDate dateFrom = LocalDate.of(2020, 5, 10);

        //when
        LocalDate[] result = DateUtils.checkAnyDateIsNull(dateFrom, null);

        //then
        assertThat(result[0]).isEqualTo(dateFrom);
        assertThat(result[1]).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldSetBothDatesWhenBothAreNull() {
        //given, when
        LocalDate[] result = DateUtils.checkAnyDateIsNull(null, null);

        //then
        assertThat(result[0]).isEqualTo(LocalDate.of(1970, 1, 1));
        assertThat(result[1]).isEqualTo(LocalDate.now());
    }
}