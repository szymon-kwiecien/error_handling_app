package pl.error_handling_app.report.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record RemainingTime(long days, long hours, long minutes, boolean isExpired) {

    public static RemainingTime calculate(LocalDateTime target) {
        if (target == null) {
            return new RemainingTime(0, 0, 0, true);
        }
        Duration duration = Duration.between(LocalDateTime.now(), target);
        if (duration.isNegative() || duration.isZero()) {
            return new RemainingTime(0, 0, 0, true);
        }
        return new RemainingTime(
                duration.toDays(),
                duration.toHoursPart(),
                duration.toMinutesPart(),
                false
        );
    }

    public String format() {
        if (isExpired) {
            return "-";
        }
        List<String> parts = new ArrayList<>();
        if (days > 0) parts.add(days + "d");
        if (hours > 0) parts.add(hours + "godz.");
        if (minutes > 0) parts.add(minutes + "min.");

        return parts.isEmpty() ? "0 min." : String.join(" ", parts);
    }
}