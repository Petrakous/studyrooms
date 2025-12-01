package gr.hua.dit.studyrooms.availability;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.ReservationStatus;
import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpaceAvailabilityService {

    private final ReservationRepository reservationRepository;

    public SpaceAvailabilityService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<TimeSlotAvailability> getDailyAvailability(StudySpace space, LocalDate date) {

        List<TimeSlotAvailability> list = new ArrayList<>();

        // ---- 1. LOAD opening hours ----
        LocalTime open = space.getOpenTime();
        LocalTime close = space.getCloseTime();

        // ---- 2. SAFETY: avoid infinite loops (close <= open) ----
        // Example: open 00:00, close 23:59 or someone sets wrong hours
        if (!close.isAfter(open)) {
            close = LocalTime.of(23, 59);  // treat as full-day open
        }

        // ---- 3. SAFETY: limit maximum loop time to 24h to avoid infinite loops ----
        LocalTime endLimit = open.plusHours(24);
        if (close.isAfter(endLimit)) {
            close = endLimit.minusMinutes(1);
        }

        // ---- 4. Fetch active reservations ----
        List<ReservationStatus> active = List.of(
                ReservationStatus.PENDING,
                ReservationStatus.CONFIRMED
        );

        List<Reservation> reservations =
                reservationRepository.findByStudySpaceAndDateAndStatusIn(space, date, active);

        // ---- 5. Loop the day in 30-minute slots ----
        for (LocalTime t = open; t.isBefore(close); t = t.plusMinutes(30)) {

            LocalTime slotStart = t;
            LocalTime slotEnd = t.plusMinutes(30);

            // Don't overflow beyond closing time
            if (!slotEnd.isBefore(close)) {
                slotEnd = close;
            }

            boolean occupied = false;

            for (Reservation r : reservations) {
                if (timesOverlap(slotStart, slotEnd, r.getStartTime(), r.getEndTime())) {
                    occupied = true;
                    break;
                }
            }

            list.add(new TimeSlotAvailability(slotStart, slotEnd, occupied));
        }

        return list;
    }

    private boolean timesOverlap(LocalTime aStart, LocalTime aEnd,
                                 LocalTime bStart, LocalTime bEnd) {
        return !aEnd.isBefore(bStart) && !bEnd.isBefore(aStart);
    }
}
