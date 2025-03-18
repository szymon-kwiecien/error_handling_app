package pl.error_handling_app.company;

public class CompanyDto {

    private Long id;
    private String name;
    private int timeToFirstRespond;
    private int timeToResolve;



    public CompanyDto(Long id, String name, int timeToFirstRespond, int timeToResolve) {
        this.id = id;
        this.name = name;
        this.timeToFirstRespond = timeToFirstRespond;
        this.timeToResolve = timeToResolve;
    }

    public CompanyDto() {
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

    public int getTimeToFirstRespond() {
        return timeToFirstRespond;
    }

    public void setTimeToFirstRespond(int timeToFirstRespond) {
        this.timeToFirstRespond = timeToFirstRespond;
    }

    public int getTimeToResolve() {
        return timeToResolve;
    }

    public void setTimeToResolve(int timeToResolve) {
        this.timeToResolve = timeToResolve;
    }
}
