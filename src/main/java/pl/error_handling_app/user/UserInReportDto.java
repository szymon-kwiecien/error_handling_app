package pl.error_handling_app.user;

public class UserInReportDto {

    private Long id;
    private String email;
    private String companyName;

    public UserInReportDto(Long id, String email, String companyName) {
        this.id = id;
        this.email = email;
        this.companyName = companyName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
