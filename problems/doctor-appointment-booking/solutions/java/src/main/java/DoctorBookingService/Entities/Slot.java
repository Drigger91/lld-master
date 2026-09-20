package DoctorBookingService.Entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class Slot {
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    private final LocalDateTime startTime;

    public LocalDateTime getEndTime() {
        return endTime;
    }

    private final LocalDateTime endTime;
    private final int duration;

    public Slot(LocalDateTime startTime, int duration) {
        this.startTime = startTime;
        this.endTime = startTime.plusMinutes(duration);
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Slot other)) return false;
        return duration == other.duration && startTime.equals(other.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, duration);
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d-%02d:%02d", startTime.getHour(), startTime.getMinute(), endTime.getHour(), endTime.getMinute());
    }

}
