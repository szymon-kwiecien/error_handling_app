package pl.error_handling_app.report.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.error_handling_app.exception.CategoryAlreadyExistsException;
import pl.error_handling_app.exception.CategoryNotFoundException;
import pl.error_handling_app.report.dto.ReportCategoryDto;
import pl.error_handling_app.report.entity.ReportCategory;
import pl.error_handling_app.report.repository.ReportCategoryRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportCategoryServiceTest {

    @Mock
    private ReportCategoryRepository reportCategoryRepository;

    @InjectMocks
    private ReportCategoryService reportCategoryService;

    @Test
    void shouldAddCategorySuccessfullyWhenNameIsNotTaken() {
        //given
        ReportCategoryDto dto = new ReportCategoryDto(null, "Nowa Kategoria");
        when(reportCategoryRepository.findByName("Nowa Kategoria")).thenReturn(Optional.empty());

        //when
        reportCategoryService.addCategory(dto);

        //then
        ArgumentCaptor<ReportCategory> captor = ArgumentCaptor.forClass(ReportCategory.class);
        verify(reportCategoryRepository, times(1)).save(captor.capture());

        ReportCategory savedCategory = captor.getValue();
        assertThat(savedCategory.getName()).isEqualTo("Nowa Kategoria");
    }

    @Test
    void shouldThrowExceptionWhenAddingCategoryWithExistingName() {
        //given
        ReportCategoryDto dto = new ReportCategoryDto(null, "Istniejąca Kategoria");
        ReportCategory existingCategory = new ReportCategory();
        existingCategory.setId(1L);
        existingCategory.setName("Istniejąca Kategoria");

        when(reportCategoryRepository.findByName("Istniejąca Kategoria")).thenReturn(Optional.of(existingCategory));

        //when, then
        assertThrows(CategoryAlreadyExistsException.class, () -> reportCategoryService.addCategory(dto));

        verify(reportCategoryRepository, never()).save(any());
    }

    @Test
    void shouldEditCategorySuccessfullyWhenNameIsNotTaken() {
        //given
        Long categoryId = 1L;
        ReportCategoryDto dto = new ReportCategoryDto(categoryId, "Zmieniona Nazwa");

        ReportCategory existingCategory = new ReportCategory();
        existingCategory.setId(categoryId);
        existingCategory.setName("Stara Nazwa");

        when(reportCategoryRepository.findByName("Zmieniona Nazwa")).thenReturn(Optional.empty());
        when(reportCategoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        //when
        reportCategoryService.editCategory(categoryId, dto);

        //then
        assertThat(existingCategory.getName()).isEqualTo("Zmieniona Nazwa");
    }

    @Test
    void shouldThrowExceptionWhenEditingCategoryToNameTakenByAnotherCategory() {
        //given
        Long editingCategoryId = 1L;
        ReportCategoryDto dto = new ReportCategoryDto(editingCategoryId, "Zajęta Nazwa");

        ReportCategory anotherCategory = new ReportCategory();
        anotherCategory.setId(2L);
        anotherCategory.setName("Zajęta Nazwa");

        when(reportCategoryRepository.findByName("Zajęta Nazwa")).thenReturn(Optional.of(anotherCategory));

        //when, then
        assertThrows(CategoryAlreadyExistsException.class, () -> reportCategoryService.editCategory(editingCategoryId, dto));
    }

    @Test
    void shouldThrowExceptionWhenEditingNonExistentCategory() {
        //given
        Long nonExistentId = 99L;
        ReportCategoryDto dto = new ReportCategoryDto(nonExistentId, "testowa nazwa");

        when(reportCategoryRepository.findByName("testowa nazwa")).thenReturn(Optional.empty());
        when(reportCategoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        //when, then
        assertThrows(CategoryNotFoundException.class, () -> reportCategoryService.editCategory(nonExistentId, dto));
    }

    @Test
    void shouldDeleteCategorySuccessfully() {
        //given
        Long categoryId = 1L;
        ReportCategory existingCategory = new ReportCategory();
        existingCategory.setId(categoryId);

        when(reportCategoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        //when
        reportCategoryService.deleteCategory(categoryId);

        //then
        verify(reportCategoryRepository, times(1)).delete(existingCategory);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCategory() {
        //given
        Long nonExistentId = 99L;
        when(reportCategoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        //when, then
        assertThrows(CategoryNotFoundException.class, () -> reportCategoryService.deleteCategory(nonExistentId));

        verify(reportCategoryRepository, never()).delete(any());
    }
}