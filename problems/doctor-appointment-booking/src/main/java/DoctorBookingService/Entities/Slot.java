package DoctorBookingService.Entities;

import java.time.LocalDateTime;

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
    public String toString() {
        return startTime.getHour() + ":" + startTime.getMinute() + "-" + endTime.getHour() + ":" + endTime.getMinute();
    }

}
