package gr.hua.dit.studyrooms.availability;

import java.time.LocalTime;

public class TimeSlotView {

    private final LocalTime start;
    private final LocalTime end;
    private final boolean booked;

    public TimeSlotView(LocalTime start, LocalTime end, boolean booked) {
        this.start = start;
        this.end = end;
        this.booked = booked;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public boolean isBooked() {
        return booked;
    }
}
