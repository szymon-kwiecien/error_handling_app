package pl.error_handling_app.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class NewReportDto {

    @NotBlank(message = "Tytuł nie może być pusty")
    @Size(min = 10, max = 80, message = "Tytuł musi mieć długość pomiędzy 10 a 80 znaków")
    private String title;
    @NotBlank(message = "Opis nie może być pusty")
    @Size(min = 20, max = 2000, message = "Opis musi mieć długość pomiędzy 20 a 2000 znaków")
    private String description;
    @NotNull(message = "Kategoria musi być wybrana")
    private Long categoryId;
    private List<MultipartFile> file = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<MultipartFile> getFile() {
        return file;
    }

    public void setFile(List<MultipartFile> file) {
        this.file = file;
    }
}
