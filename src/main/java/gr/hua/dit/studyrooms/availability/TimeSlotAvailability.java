package gr.hua.dit.studyrooms.availability;

import java.time.LocalTime;

/**
 * Represents the availability status of a single 30-minute time slot for a study space.
 * 
 * This immutable data class encapsulates the time boundaries and booking status of a slot.
 * It is used internally by the availability calculation service and can be transformed into
 * view models for API responses.
 * 
 * All fields are final, ensuring thread-safety and preventing accidental state modifications
 * after object creation.
 */
public class TimeSlotAvailability {

    /** The start time of this slot */
    private final LocalTime start;
    
    /** The end time of this slot */
    private final LocalTime end;
    
    /** True if this slot is fully booked (at capacity); false if available for new reservations */
    private final boolean occupied;

    /**
     * Constructs a time slot availability record with the given boundaries and occupancy status.
     * 
     * @param start the start time of the slot (e.g., 9:00 AM)
     * @param end the end time of the slot (e.g., 9:30 AM)
     * @param occupied true if the slot is at capacity and cannot accept new reservations, false otherwise
     */
    public TimeSlotAvailability(LocalTime start, LocalTime end, boolean occupied) {
        this.start = start;
        this.end = end;
        this.occupied = occupied;
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
     * Indicates whether this slot is fully booked.
     * 
     * @return true if the slot has reached capacity and no new reservations can be made, false otherwise
     */
    public boolean isOccupied() {
        return occupied;
    }
}
