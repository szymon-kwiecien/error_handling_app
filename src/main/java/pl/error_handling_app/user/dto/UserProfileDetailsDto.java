package pl.error_handling_app.user.dto;

public class UserProfileDetailsDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String companyName;
    private String companyTimeToFirstRespond;
    private String companyTimeToResolve;

    public UserProfileDetailsDto(Long id, String firstName, String lastName, String email, String role, String companyName, String companyTimeToFirstRespond, String companyTimeToResolve) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.companyName = companyName;
        this.companyTimeToFirstRespond = companyTimeToFirstRespond;
        this.companyTimeToResolve = companyTimeToResolve;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }


    public String getCompanyTimeToFirstRespond() {
        return companyTimeToFirstRespond;
    }

    public void setCompanyTimeToFirstRespond(String companyTimeToFirstRespond) {
        this.companyTimeToFirstRespond = companyTimeToFirstRespond;
    }

    public String getCompanyTimeToResolve() {
        return companyTimeToResolve;
    }

    public void setCompanyTimeToResolve(String companyTimeToResolve) {
        this.companyTimeToResolve = companyTimeToResolve;
    }
}
