package pl.error_handling_app.report;

import java.util.Objects;

public class ReportCategoryDto {

    private Long id;
    private String name;

    public ReportCategoryDto() {
    }

    public ReportCategoryDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
