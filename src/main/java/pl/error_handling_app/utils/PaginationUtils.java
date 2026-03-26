package pl.error_handling_app.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    private static final int DEFAULT_SIZE = 5;
    private static final int MAX_SIZE = 20;

    private PaginationUtils() {
        throw new UnsupportedOperationException("Nie można tworzyć obiektow tej klasy.");
    }

    public static Pageable createPageable(int page, int size, Sort sort) {
        int validatedPage = Math.max(1, page);
        int validatedSize = (size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        return PageRequest.of(validatedPage - 1, validatedSize, sort);
    }
}