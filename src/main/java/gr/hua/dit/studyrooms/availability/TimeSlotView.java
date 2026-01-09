package gr.hua.dit.studyrooms.availability;

import java.time.LocalTime;

/**
 * View model for presenting time slot booking information to clients via API responses.
 * 
 * This immutable data class is designed for serialization to JSON and represents a single
 * 30-minute time slot with its availability status. It serves as the transfer object between
 * the backend service layer and REST controllers.
 * 
 * All fields are final, ensuring thread-safety and compatibility with serialization frameworks
 * like Jackson that expect immutable objects.
 */
public class TimeSlotView {

    /** The start time of this slot */
    private final LocalTime start;
    
    /** The end time of this slot */
    private final LocalTime end;
    
    /** True if this slot has been booked and is unavailable; false if it remains open for reservation */
    private final boolean booked;

    /**
     * Constructs a time slot view record with the given boundaries and booking status.
     * 
     * @param start the start time of the slot (e.g., 9:00 AM)
     * @param end the end time of the slot (e.g., 9:30 AM)
     * @param booked true if the slot has been reserved and is unavailable, false if open for booking
     */
    public TimeSlotView(LocalTime start, LocalTime end, boolean booked) {
        this.start = start;
        this.end = end;
        this.booked = booked;
    }

    /**
     * Returns the start time of this slot.
     * 
     * @return the start time as a LocalTime object
     */
    public LocalTime getStart() {
        return start;
    }

    /**
     * Returns the end time of this slot.
     * 
     * @return the end time as a LocalTime object
     */
    public LocalTime getEnd() {
        return end;
    }

    /**
     * Indicates whether this slot has been booked.
     * 
     * @return true if the slot is reserved and unavailable, false if it remains open for new reservations
     */
    public boolean isBooked() {
        return booked;
    }
}
