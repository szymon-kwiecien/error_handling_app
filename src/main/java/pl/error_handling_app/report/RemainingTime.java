package pl.error_handling_app.report;

public class RemainingTime {
    private long days;
    private long hours;
    private long minutes;
    private boolean isExpired;

    public RemainingTime(long days, long hours, long minutes, boolean isExpired) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.isExpired = isExpired;
    }

    public long getDays() {
        return days;
    }

    public long getHours() {
        return hours;
    }

    public long getMinutes() {
        return minutes;
    }

    public boolean isExpired() {
        return isExpired;
    }
}
