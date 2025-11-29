package gr.hua.dit.studyrooms.availability;

import gr.hua.dit.studyrooms.entity.Reservation;
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

    // πόσα λεπτά κρατάει κάθε slot (π.χ. 30')
    private static final int SLOT_MINUTES = 30;

    public SpaceAvailabilityService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<TimeSlotView> getDailyAvailability(StudySpace space, LocalDate date) {

        List<Reservation> reservations =
                reservationRepository.findByStudySpaceAndDate(space, date);

        LocalTime start = space.getOpenTime();
        LocalTime end = space.getCloseTime();

        List<TimeSlotView> slots = new ArrayList<>();

        LocalTime slotStart = start;
        while (slotStart.isBefore(end)) {
            LocalTime slotEnd = slotStart.plusMinutes(SLOT_MINUTES);
            if (slotEnd.isAfter(end)) {
                slotEnd = end;
            }

            boolean booked = isSlotBooked(slotStart, slotEnd, reservations);

            slots.add(new TimeSlotView(slotStart, slotEnd, booked));
            slotStart = slotEnd;
        }

        return slots;
    }

    private boolean isSlotBooked(LocalTime slotStart,
                                 LocalTime slotEnd,
                                 List<Reservation> reservations) {

        // overlap: res.start < slotEnd && res.end > slotStart
        return reservations.stream().anyMatch(r ->
                r.getStartTime().isBefore(slotEnd) &&
                        r.getEndTime().isAfter(slotStart)
        );
    }
}
