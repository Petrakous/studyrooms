package gr.hua.dit.studyrooms.service.impl;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.ReservationStatus;
import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.external.HolidayApiPort;
import gr.hua.dit.studyrooms.repository.ReservationRepository;
import gr.hua.dit.studyrooms.repository.StudySpaceRepository;
import gr.hua.dit.studyrooms.repository.UserRepository;
import gr.hua.dit.studyrooms.service.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    // Μέχρι 3 ενεργές κρατήσεις ανά μέρα ανά φοιτητή
    private static final int MAX_RESERVATIONS_PER_DAY = 3;

    // Μέγιστη διάρκεια μίας κράτησης: 2 ώρες (120 λεπτά)
    private static final int MAX_RESERVATION_DURATION_MINUTES = 120;

    private final ReservationRepository reservationRepository;
    private final StudySpaceRepository studySpaceRepository;
    private final UserRepository userRepository;
    private final HolidayApiPort holidayApiPort;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            StudySpaceRepository studySpaceRepository,
            UserRepository userRepository, HolidayApiPort holidayApiPort) {

        this.reservationRepository = reservationRepository;
        this.studySpaceRepository = studySpaceRepository;
        this.userRepository = userRepository;
        this.holidayApiPort = holidayApiPort;
    }

    @Override
    public List<Reservation> getReservationsForUser(User user) {
        return reservationRepository.findByUser(user);
    }

    @Override
    public List<Reservation> getReservationsForDate(LocalDate date) {
        return reservationRepository.findByDate(date);
    }

    @Override
    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation createReservation(User user, Long studySpaceId,
                                         LocalDate date, LocalTime startTime, LocalTime endTime) {

        // 🔒 Penalty check: αν ο χρήστης είναι μπλοκαρισμένος, δεν κάνουμε κράτηση
        if (user.getPenaltyUntil() != null &&
                !user.getPenaltyUntil().isBefore(LocalDate.now())) {
            throw new IllegalStateException(
                    "You cannot make a reservation until " + user.getPenaltyUntil()
            );
        }

        // 0. Έλεγχος αργίας μέσω external API
        if (holidayApiPort.isHoliday(date)) {
            throw new IllegalStateException("Reservations are not allowed on public holidays.");
        }

        // 1. Φόρτωση χώρου
        StudySpace space = studySpaceRepository.findById(studySpaceId)
                .orElseThrow(() -> new IllegalArgumentException("StudySpace not found: " + studySpaceId));

        // 2. Αν ο χώρος έχει κλείσει από το προσωπικό για εκείνη την ημέρα, δεν επιτρέπονται κρατήσεις
        if (reservationRepository.existsByStudySpaceAndDateAndStatus(
                space,
                date,
                ReservationStatus.CANCELLED_BY_STAFF
        )) {
            throw new IllegalStateException(
                    "This study space has been closed by staff for the selected date. " +
                            "Please choose another date or space."
            );
        }

        // 3. Όχι πάνω από Χ ενεργές κρατήσεις / μέρα για τον ίδιο user
        //    Ενεργές θεωρούμε PENDING & CONFIRMED
        List<ReservationStatus> activeStatuses = List.of(
                ReservationStatus.PENDING,
                ReservationStatus.CONFIRMED
        );

        long activeCountForDay = reservationRepository
                .countByUserAndDateAndStatusIn(user, date, activeStatuses);

        if (activeCountForDay >= MAX_RESERVATIONS_PER_DAY) {
            throw new IllegalStateException(
                    "You have reached the maximum number of active reservations (" +
                            MAX_RESERVATIONS_PER_DAY + ") for this day."
            );
        }

        // 4. Έλεγχος ωραρίου χώρου
        if (startTime.isBefore(space.getOpenTime()) || endTime.isAfter(space.getCloseTime())) {
            throw new IllegalStateException("Reservation time outside study space opening hours");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalStateException("End time must be after start time");
        }

        // 5. Κανόνας: Μέγιστη διάρκεια κράτησης = 2 ώρες
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes > MAX_RESERVATION_DURATION_MINUTES) {
            throw new IllegalStateException("Maximum duration per reservation is 2 hours.");
        }

        // 6. Κανόνας: δεν επιτρέπονται επικαλυπτόμενες κρατήσεις για τον ίδιο χώρο
        long overlapping = reservationRepository.countOverlappingReservations(
                space,
                date,
                startTime,
                endTime,
                activeStatuses // η λίστα PENDING/CONFIRMED που ορίσαμε πιο πάνω
        );

        if (overlapping > 0) {
            throw new IllegalStateException(
                    "This study space is already reserved for the selected time range."
            );
        }

        // 7. Έλεγχος διαθεσιμότητας (capacity) – απλά με βάση πόσες κρατήσεις έχει ο χώρος τη μέρα αυτή
        List<Reservation> reservationsForSpaceAndDate =
                reservationRepository.findByStudySpaceAndDate(space, date);

        if (reservationsForSpaceAndDate.size() >= space.getCapacity()) {
            throw new IllegalStateException("No available seats for this study space on this date");
        }

        // 8. Δημιουργία και αποθήκευση κράτησης
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setStudySpace(space);
        reservation.setDate(date);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(ReservationStatus.CONFIRMED); // ή PENDING αν θέλεις approval workflow

        return reservationRepository.save(reservation);
    }

    @Override
    public void cancelReservation(Long reservationId, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    public void cancelReservationAsStaff(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        reservation.setStatus(ReservationStatus.CANCELLED_BY_STAFF);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public int cancelByStaffForSpaceAndDate(Long spaceId, LocalDate date) {

        StudySpace space = studySpaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        List<Reservation> reservations =
                reservationRepository.findByStudySpaceAndDate(space, date);

        int cancelled = 0;
        for (Reservation r : reservations) {
            if (r.getStatus() != ReservationStatus.CANCELLED
                    && r.getStatus() != ReservationStatus.CANCELLED_BY_STAFF) {

                r.setStatus(ReservationStatus.CANCELLED_BY_STAFF);
                cancelled++;
            }
        }

        return cancelled;
    }

    @Override
    public void markNoShow(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        User user = reservation.getUser();

        // Μόνο αν η κράτηση δεν είναι ήδη cancelled
        if (reservation.getStatus() == ReservationStatus.CANCELLED ||
                reservation.getStatus() == ReservationStatus.CANCELLED_BY_STAFF ||
                reservation.getStatus() == ReservationStatus.NO_SHOW) {
            return;
        }

        // 3 ημέρες penalty από σήμερα
        user.setPenaltyUntil(LocalDate.now().plusDays(3));

        // Μαρκάρουμε τη κράτηση ως NO_SHOW
        reservation.setStatus(ReservationStatus.NO_SHOW);

        reservationRepository.save(reservation);
        userRepository.save(user);
    }

}
