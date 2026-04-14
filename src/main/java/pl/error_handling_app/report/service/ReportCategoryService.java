package pl.error_handling_app.report.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.error_handling_app.exception.CategoryAlreadyExistsException;
import pl.error_handling_app.exception.CategoryNotFoundException;
import pl.error_handling_app.report.dto.ReportCategoryDto;
import pl.error_handling_app.report.entity.ReportCategory;
import pl.error_handling_app.report.repository.ReportCategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ReportCategoryService {

    private final ReportCategoryRepository reportCategoryRepository;

    public ReportCategoryService(ReportCategoryRepository reportCategoryRepository) {
        this.reportCategoryRepository = reportCategoryRepository;
    }

    public List<ReportCategoryDto> getAllCategories() {
        return reportCategoryRepository.findAll().stream().map(this::map).toList();
    }

    public Page<ReportCategoryDto> getPagedCategories(Pageable pageable) {
        return reportCategoryRepository.findAll(pageable).map(this::map);
    }

    public Optional<ReportCategory> getCategoryById(Long id) {
        return reportCategoryRepository.findById(id);
    }

    public Optional<ReportCategory> getCategoryByName(String name) {
        return reportCategoryRepository.findByName(name);
    }

    public void addCategory(ReportCategoryDto categoryDto) {
        isCategoryNameTaken(categoryDto.name(), null);
        ReportCategory reportCategory = new ReportCategory();
        reportCategory.setName(categoryDto.name());
        reportCategoryRepository.save(reportCategory);
    }

    @Transactional
    public void editCategory(Long id, ReportCategoryDto categoryDto) {
        String newCategoryName = categoryDto.name();
        isCategoryNameTaken(newCategoryName, id);
        ReportCategory reportCategory = reportCategoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Kategoria nie została znaleziona."));
        reportCategory.setName(newCategoryName);
    }

    public void deleteCategory(Long id) {
        ReportCategory reportCategory = reportCategoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Kategoria nie istnieje."));
        reportCategoryRepository.delete(reportCategory);
    }

    private void isCategoryNameTaken(String categoryName, Long currentCategoryId) {
        reportCategoryRepository.findByName(categoryName).ifPresent(existingCategory -> {
            if (!existingCategory.getId().equals(currentCategoryId)) {
                throw new CategoryAlreadyExistsException("Kategoria o takiej nazwie już istnieje");
            }
        });
    }

    private ReportCategoryDto map(ReportCategory reportCategory) {
        return new ReportCategoryDto(reportCategory.getId(), reportCategory.getName());
    }
}
