package gr.hua.dit.studyrooms.availability;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.ReservationStatus;
import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for calculating and retrieving available time slots for study spaces.
 * 
 * This service generates a daily availability schedule by dividing operating hours
 * into 30-minute slots and marking each as occupied (at capacity) or available.
 * Occupancy is determined by counting overlapping reservations against the space's capacity.
 */
@Service
public class SpaceAvailabilityService {

    private final ReservationRepository reservationRepository;

    /**
     * Constructs the service with a repository for accessing reservation data.
     * 
     * @param reservationRepository repository for querying reservations from the database
     */
    public SpaceAvailabilityService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Generates a list of 30-minute availability slots for a given space on a specific date.
     * 
     * This method calculates availability by:
     * 1. Extracting the space's operating hours for the given date
     * 2. Validating that opening and closing times make sense (with fallback logic for edge cases)
     * 3. Fetching all active (PENDING or CONFIRMED) reservations for that date
     * 4. Dividing the operating day into 30-minute slots
     * 5. Checking each slot for overlapping reservations to determine occupancy
     * 
     * @param space the study space to check availability for
     * @param date the date to generate availability for
     * @return a list of TimeSlotAvailability objects representing each 30-minute slot
     */
    public List<TimeSlotAvailability> getDailyAvailability(StudySpace space, LocalDate date) {

        List<TimeSlotAvailability> list = new ArrayList<>();

        // ---- 1. LOAD opening hours ----
        // Extract opening and closing times from the space configuration
        LocalTime open = space.getOpenTime();
        LocalTime close = space.getCloseTime();

        // Combine the date with opening/closing times to create boundary timestamps for the day
        LocalDateTime dayStart = date.atTime(open);
        LocalDateTime dayEnd = date.atTime(close);

        // ---- 2. SAFETY: avoid infinite loops (close <= open) ----
        // Validates that closing time is after opening time. If not (e.g., due to data entry error
        // or intentional 24-hour operation flagged with identical times), treat as nearly full day.
        // This prevents the loop from never terminating when dayEnd <= dayStart.
        if (!dayEnd.isAfter(dayStart)) {
            dayEnd = dayStart.plusHours(23).plusMinutes(59);  // treat as full-day open (23:59)
        }

        // ---- 3. SAFETY: limit maximum loop time to 24h to avoid infinite loops ----
        // Enforces a hard cap on how long a day can be. If somehow closing time extends beyond
        // 24 hours from opening (unlikely but possible with malformed data), truncate it.
        // This prevents runaway loops that could cause performance issues or memory exhaustion.
        LocalDateTime endLimit = dayStart.plusHours(24);
        if (dayEnd.isAfter(endLimit)) {
            dayEnd = endLimit.minusMinutes(1);  // set to one minute before the 24-hour limit
        }

        // ---- 4. Fetch active reservations ----
        // Define which reservation statuses should count toward occupancy.
        // Only PENDING and CONFIRMED reservations block availability; CANCELLED or COMPLETED do not.
        List<ReservationStatus> active = List.of(
                ReservationStatus.PENDING,
                ReservationStatus.CONFIRMED
        );

        // Query the repository for all active reservations for this space on the given date
        List<Reservation> reservations =
                reservationRepository.findByStudySpaceAndDateAndStatusIn(space, date, active);

        // ---- 5. Loop the day in 30-minute slots ----
        // Iterate through the day starting at opening time, advancing in 30-minute increments
        // until the closing time is reached.
        for (LocalDateTime t = dayStart; t.isBefore(dayEnd); t = t.plusMinutes(30)) {

            // Define the current slot's boundaries
            LocalDateTime slotStart = t;
            LocalDateTime slotEnd = t.plusMinutes(30);

            // Prevent the final slot from extending past closing time.
            // For example, if closing is at 5:00 PM and the last slot would end at 5:30 PM,
            // truncate it to 5:00 PM instead.
            if (!slotEnd.isBefore(dayEnd)) {
                slotEnd = dayEnd;
            }

            // Extract just the time portion (without date) for overlap checking
            LocalTime slotStartTime = slotStart.toLocalTime();
            LocalTime slotEndTime = slotEnd.toLocalTime();

            // Count how many active reservations overlap with this slot.
            // A reservation is considered "concurrent" if its time range overlaps with this slot.
            long concurrent = reservations.stream()
                    .filter(r -> timesOverlap(slotStartTime, slotEndTime, r.getStartTime(), r.getEndTime()))
                    .count();

            // A slot is occupied if the number of concurrent reservations meets or exceeds capacity.
            // This ensures the slot cannot accept further bookings when at or over capacity.
            boolean occupied = concurrent >= space.getCapacity();

            // Create and store the availability record for this slot
            list.add(new TimeSlotAvailability(slotStart.toLocalTime(), slotEnd.toLocalTime(), occupied));
        }

        return list;
    }

    /**
     * Determines whether two time ranges overlap.
     * 
     * Two time ranges overlap if the start of one is before the end of the other,
     * AND the start of the second is before the end of the first.
     * This check excludes exact boundary matches (e.g., one slot ending at 2:00 PM
     * and another starting at 2:00 PM do NOT overlap).
     * 
     * @param aStart start time of the first range
     * @param aEnd end time of the first range
     * @param bStart start time of the second range
     * @param bEnd end time of the second range
     * @return true if the ranges overlap, false otherwise
     */
    private boolean timesOverlap(LocalTime aStart, LocalTime aEnd,
                                 LocalTime bStart, LocalTime bEnd) {
        return bEnd.isAfter(aStart) && bStart.isBefore(aEnd);
    }
}
