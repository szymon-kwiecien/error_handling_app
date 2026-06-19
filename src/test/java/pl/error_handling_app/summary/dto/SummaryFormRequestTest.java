package pl.error_handling_app.summary.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SummaryFormRequestTest {

    @Test
    void shouldSetDefaultValuesWhenFieldsAreNull() {
        //given, when
        SummaryFormRequest request = new SummaryFormRequest(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                "Błędy IT",
                null,
                null,
                "dateAdded,desc",
                null,
                null
        );

        //then
        assertThat(request.user()).isEqualTo("all");
        assertThat(request.showReportsTable()).isFalse();
        assertThat(request.showCharts()).isFalse();

        assertThat(request.categoryName()).isEqualTo("Błędy IT");
        assertThat(request.dateFrom()).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    void shouldKeepProvidedValuesWhenTheyAreNotNull() {
        //given, when
        SummaryFormRequest request = new SummaryFormRequest(
                null,
                null,
                null,
                null,
                "testowy@test.pl",
                null,
                true,
                true
        );

        //then
        assertThat(request.user()).isEqualTo("testowy@test.pl");
        assertThat(request.showReportsTable()).isTrue();
        assertThat(request.showCharts()).isTrue();
    }
}