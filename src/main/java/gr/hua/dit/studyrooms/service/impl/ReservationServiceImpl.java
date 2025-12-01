package gr.hua.dit.studyrooms.service.impl;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.ReservationStatus;
import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.external.HolidayApiPort;
import gr.hua.dit.studyrooms.repository.ReservationRepository;
import gr.hua.dit.studyrooms.repository.StudySpaceRepository;
import gr.hua.dit.studyrooms.repository.UserRepository;
import gr.hua.dit.studyrooms.service.NotificationService;
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
    private final NotificationService notificationService;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  StudySpaceRepository studySpaceRepository,
                                  HolidayApiPort holidayApiPort,
                                  NotificationService notificationService,
                                  UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.studySpaceRepository = studySpaceRepository;
        this.userRepository = userRepository;
        this.holidayApiPort = holidayApiPort;
        this.notificationService = notificationService;
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
        List<ReservationStatus> activeStatuses = List.of(
                ReservationStatus.PENDING,
                ReservationStatus.CONFIRMED
        );

        StudySpace space = loadStudySpace(studySpaceId);
        checkNotInPast(date, startTime);
        checkHoliday(date);
        checkSpaceClosedByStaff(space, date);
        checkMaxReservationsPerDay(user, date, activeStatuses);
        checkOpeningHours(space, startTime, endTime);
        checkDurationWithinLimit(startTime, endTime);
        checkOverlap(space, date, startTime, endTime, activeStatuses);
        checkCapacity(space, date, activeStatuses);

        Reservation reservation = persistReservation(user, space, date, startTime, endTime);
        notificationService.notifyReservationCreated(reservation);
        return reservation;
    }

    @Override
    public void cancelReservation(Long reservationId, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        // προς το παρόν: μόνο ο ίδιος ο χρήστης μπορεί να ακυρώσει τη δική του κράτηση
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You cannot cancel another user's reservation.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        notificationService.notifyReservationCancelled(reservation, false);
    }

    @Override
    public void cancelReservationAsStaff(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        reservation.setStatus(ReservationStatus.CANCELLED_BY_STAFF);
        reservationRepository.save(reservation);
        notificationService.notifyReservationCancelled(reservation, true);
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

        if (cancelled > 0) {
            reservationRepository.saveAll(reservations);
        }

        return cancelled;
    }

    private void checkNotInPast(LocalDate date, LocalTime startTime) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new IllegalStateException("You cannot reserve in the past.");
        }

        if (date.isEqual(today) && startTime.isBefore(LocalTime.now())) {
            throw new IllegalStateException("This start time has already passed for today.");
        }
    }

    private void checkHoliday(LocalDate date) {
        if (holidayApiPort.isHoliday(date)) {
            throw new IllegalStateException("Reservations are not allowed on public holidays.");
        }
    }

    private StudySpace loadStudySpace(Long studySpaceId) {
        return studySpaceRepository.findById(studySpaceId)
                .orElseThrow(() -> new IllegalArgumentException("StudySpace not found: " + studySpaceId));
    }

    private void checkSpaceClosedByStaff(StudySpace space, LocalDate date) {
        if (reservationRepository.existsByStudySpaceAndDateAndStatus(space, date, ReservationStatus.CANCELLED_BY_STAFF)) {
            throw new IllegalStateException(
                    "This study space has been closed by staff for the selected date. " +
                            "Please choose another date or space."
            );
        }
    }

    private void checkMaxReservationsPerDay(User user, LocalDate date, List<ReservationStatus> activeStatuses) {
        long activeCountForDay = reservationRepository.countByUserAndDateAndStatusIn(user, date, activeStatuses);
        if (activeCountForDay >= MAX_RESERVATIONS_PER_DAY) {
            throw new IllegalStateException(
                    "You have reached the maximum number of active reservations (" +
                            MAX_RESERVATIONS_PER_DAY + ") for this day."
            );
        }
    }

    private void checkOpeningHours(StudySpace space, LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(space.getOpenTime()) || endTime.isAfter(space.getCloseTime())) {
            throw new IllegalStateException("Reservation time outside study space opening hours");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalStateException("End time must be after start time");
        }
    }

    private void checkDurationWithinLimit(LocalTime startTime, LocalTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes > MAX_RESERVATION_DURATION_MINUTES) {
            throw new IllegalStateException("Maximum duration per reservation is 2 hours.");
        }
    }

    private void checkOverlap(StudySpace space, LocalDate date, LocalTime startTime, LocalTime endTime,
                              List<ReservationStatus> activeStatuses) {
        long overlapping = reservationRepository.countOverlappingReservations(
                space,
                date,
                startTime,
                endTime,
                activeStatuses
        );

        if (overlapping > 0) {
            throw new IllegalStateException(
                    "This study space is already reserved for the selected time range."
            );
        }
    }

    private void checkCapacity(StudySpace space, LocalDate date, List<ReservationStatus> activeStatuses) {
        long activeCount = reservationRepository.countByStudySpaceAndDateAndStatusIn(space, date, activeStatuses);
        if (activeCount >= space.getCapacity()) {
            throw new IllegalStateException("No available seats for this study space on this date");
        }
    }

    private Reservation persistReservation(User user, StudySpace space, LocalDate date,
                                           LocalTime startTime, LocalTime endTime) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setStudySpace(space);
        reservation.setDate(date);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        // All business rules executed above; persist confirmed reservation.
        reservation.setStatus(ReservationStatus.CONFIRMED);

        return reservationRepository.save(reservation);
    }

    @Override
    public void markNoShow(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        // Αν είναι ήδη σε state που δεν γίνεται no-show → μην κάνεις τίποτα
        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.CANCELLED_BY_STAFF
                || reservation.getStatus() == ReservationStatus.NO_SHOW) {
            return;
        }

        User user = reservation.getUser();

        // Επιβολή penalty 3 ημερών
        user.setPenaltyUntil(LocalDate.now().plusDays(3));

        // Αλλαγή κατάστασης κράτησης
        reservation.setStatus(ReservationStatus.NO_SHOW);

        reservationRepository.save(reservation);
        userRepository.save(user);
    }
}
