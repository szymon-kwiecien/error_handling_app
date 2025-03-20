package pl.error_handling_app.report;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReportCategoryService {

    private final ReportCategoryRepository reportCategoryRepository;

    public ReportCategoryService(ReportCategoryRepository reportCategoryRepository) {
        this.reportCategoryRepository = reportCategoryRepository;
    }

    public List<ReportCategoryDto> findAllCategories() {
        return reportCategoryRepository.findAll().stream().map(this::map).toList();
    }

    public void addCategory(ReportCategoryDto categoryDto) {
        isCategoryNameTaken(categoryDto.getName());
        ReportCategory reportCategory = new ReportCategory();
        reportCategory.setName(categoryDto.getName());
        reportCategoryRepository.save(reportCategory);
    }

    @Transactional
    public void editCategory(Long id, String newCategoryName) {
        isCategoryNameTaken(newCategoryName);
        ReportCategory reportCategory = reportCategoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Kategoria nie została znaleziona."));
        reportCategory.setName(newCategoryName);
    }

    private void isCategoryNameTaken(String categoryName) {
        if(reportCategoryRepository.existsByName(categoryName)) {
            throw new IllegalArgumentException("Kategoria o takiej nazwie już istnieje.");
        }
    }

    private ReportCategoryDto map(ReportCategory reportCategory) {
        return new ReportCategoryDto(reportCategory.getId(), reportCategory.getName());
    }
}
