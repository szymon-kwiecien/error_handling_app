package pl.error_handling_app.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginationUtilsTest {

    @Test
    void shouldCreatePageableWithValidParameters() {
        //given
        int page = 2;
        int size = 15;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        //when
        Pageable pageable = PaginationUtils.createPageable(page, size, sort);

        //then
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(15);
        assertThat(pageable.getSort()).isEqualTo(sort);
    }

    @Test
    void shouldFallbackToPageOneWhenPageIsZeroOrNegative() {
        //when, then - strona numer 0
        Pageable zeroPageable = PaginationUtils.createPageable(0, 10, Sort.unsorted());
        assertThat(zeroPageable.getPageNumber()).isEqualTo(0);

        //when, then - strona ujemna
        Pageable negativePageable = PaginationUtils.createPageable(-5, 10, Sort.unsorted());
        assertThat(negativePageable.getPageNumber()).isEqualTo(0);
    }

    @Test
    void shouldFallbackToDefaultSizeWhenSizeIsLessThanOne() {
        //given, when
        Pageable zeroSizePageable = PaginationUtils.createPageable(1, 0, Sort.unsorted());
        Pageable negativeSizePageable = PaginationUtils.createPageable(1, -10, Sort.unsorted());

        //then - sprawdzenie czy rozmiar zastapiono domyslnym rozmiarem (równym 5)
        assertThat(zeroSizePageable.getPageSize()).isEqualTo(5);
        assertThat(negativeSizePageable.getPageSize()).isEqualTo(5);
    }

    @Test
    void shouldCapSizeToMaxSizeWhenSizeExceedsLimit() {
        //given, when
        Pageable oversizedPageable = PaginationUtils.createPageable(1, 50, Sort.unsorted());

        //then
        assertThat(oversizedPageable.getPageSize()).isEqualTo(20);
    }

    @Test
    void constructorShouldThrowUnsupportedOperationException() throws NoSuchMethodException {
        Constructor<PaginationUtils> constructor = PaginationUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true); //wymuszenie dostepu do prywatnego konstruktora

        //sprawdzenie czy wywołanie konstruktora skończyło się rzuceniem konkretnego typu wyjątku
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class)
                .hasRootCauseMessage("Nie można tworzyć obiektow tej klasy.");
    }
}