package gr.hua.dit.studyrooms.availability;

import java.time.LocalTime;

public class TimeSlotAvailability {

    private final LocalTime start;
    private final LocalTime end;
    private final boolean occupied;

    public TimeSlotAvailability(LocalTime start, LocalTime end, boolean occupied) {
        this.start = start;
        this.end = end;
        this.occupied = occupied;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public boolean isOccupied() {
        return occupied;
    }
}
