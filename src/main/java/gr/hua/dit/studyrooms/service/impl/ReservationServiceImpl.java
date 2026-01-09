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
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
/**
 * Implementation of ReservationService that handles all business logic for study room reservations.
 * Enforces rules such as max reservations per day, duration limits, holiday checks, penalties, and capacity.
 * Integrates with repositories and external services for persistence and notifications.
 */
public class ReservationServiceImpl implements ReservationService {


        // Maximum 3 active reservations per day per student
        private static final int MAX_RESERVATIONS_PER_DAY = 3;

        // Statuses considered "active" for reservation counting and capacity
        private static final List<ReservationStatus> ACTIVE_RESERVATION_STATUSES = List.of(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED
        );

        // Maximum duration of a reservation: 2 hours (120 minutes)
        private static final int MAX_RESERVATION_DURATION_MINUTES = 120;

    private final ReservationRepository reservationRepository;
    private final StudySpaceRepository studySpaceRepository;
    private final UserRepository userRepository;
    private final HolidayApiPort holidayApiPort;
    private final NotificationService notificationService;

    /**
     * Constructor for dependency injection.
     */
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

    /**
     * Returns all reservations for a given user.
     */
    public List<Reservation> getReservationsForUser(User user) {
        return reservationRepository.findByUser(user);
    }

    @Override

    /**
     * Returns all reservations for a specific date.
     */
    public List<Reservation> getReservationsForDate(LocalDate date) {
        return reservationRepository.findByDate(date);
    }

    @Override

    /**
     * Returns all reservations in the system.
     */
    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    @Override

    /**
     * Creates a new reservation for a user, enforcing all business rules.
     * Notifies the user upon successful creation.
     */
    public Reservation createReservation(User user, Long studySpaceId,
                                         LocalDate date, LocalTime startTime, LocalTime endTime) {
        StudySpace space = loadStudySpace(studySpaceId); // Load the study space entity
        checkUserNotPenalized(user); // Block if user is penalized
        checkNotInPast(date, startTime); // Block if reservation is in the past
        checkHoliday(date); // Block if date is a public holiday
        checkSpaceClosedByStaff(space, date); // Block if staff closed the space for that date
        checkMaxReservationsPerDay(user, date, ACTIVE_RESERVATION_STATUSES); // Enforce max per day
        checkTimeOrder(startTime, endTime); // Validate time order
        checkOpeningHours(space, startTime, endTime); // Enforce opening hours
        checkDurationWithinLimit(startTime, endTime); // Enforce max duration
        checkCapacityForTimeRange(space, date, startTime, endTime); // Enforce capacity

        Reservation reservation = persistReservation(user, space, date, startTime, endTime);
        notificationService.notifyReservationCreated(reservation); // Notify user
        return reservation;
    }

    @Override

    /**
     * Cancels a reservation by the user who owns it. Only the owner can cancel their reservation.
     * Notifies the user upon cancellation.
     */
    public void cancelReservation(Long reservationId, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        // Only the user who made the reservation can cancel it
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You cannot cancel another user's reservation.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        notificationService.notifyReservationCancelled(reservation, false);
    }

    @Override

    /**
     * Cancels a reservation as staff. Status is set to CANCELLED_BY_STAFF and user is notified.
     */
    public void cancelReservationAsStaff(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        reservation.setStatus(ReservationStatus.CANCELLED_BY_STAFF);
        reservationRepository.save(reservation);
        notificationService.notifyReservationCancelled(reservation, true);
    }

    @Override

    /**
     * Cancels all reservations for a given study space and date as staff.
     * Notifies affected users. Returns the number of cancelled reservations.
     */
    @Transactional
    public int cancelByStaffForSpaceAndDate(Long spaceId, LocalDate date) {
        StudySpace space = studySpaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        List<Reservation> reservations =
                reservationRepository.findByStudySpaceAndDate(space, date);

        int cancelled = 0;
        List<Reservation> toNotify = new ArrayList<>();
        for (Reservation r : reservations) {
            // Only cancel reservations that are not already cancelled
            if (r.getStatus() != ReservationStatus.CANCELLED
                    && r.getStatus() != ReservationStatus.CANCELLED_BY_STAFF) {

                r.setStatus(ReservationStatus.CANCELLED_BY_STAFF);
                cancelled++;
                toNotify.add(r);
            }
        }

        if (cancelled > 0) {
            reservationRepository.saveAll(reservations);
            toNotify.forEach(res -> notificationService.notifyReservationCancelled(res, true));
        }

        return cancelled;
    }


    /**
     * Throws if the reservation is in the past (date or time).
     */
    private void checkNotInPast(LocalDate date, LocalTime startTime) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new IllegalStateException("You cannot reserve in the past.");
        }

        if (date.isEqual(today) && startTime.isBefore(LocalTime.now())) {
            throw new IllegalStateException("This start time has already passed for today.");
        }
    }


    /**
     * Throws if start or end time is missing, or end is not after start.
     */
    private void checkTimeOrder(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalStateException("Start and end time are required.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalStateException("End time must be after start time.");
        }
    }


    /**
     * Throws if the date is a public holiday (uses external API).
     */
    private void checkHoliday(LocalDate date) {
        if (holidayApiPort.isHoliday(date)) {
            throw new IllegalStateException("Reservations are not allowed on public holidays.");
        }
    }


    /**
     * Loads a StudySpace by ID or throws if not found.
     */
    private StudySpace loadStudySpace(Long studySpaceId) {
        return studySpaceRepository.findById(studySpaceId)
                .orElseThrow(() -> new IllegalArgumentException("StudySpace not found: " + studySpaceId));
    }


    /**
     * Throws if the study space is closed by staff for the given date.
     */
    private void checkSpaceClosedByStaff(StudySpace space, LocalDate date) {
        if (reservationRepository.existsByStudySpaceAndDateAndStatus(space, date, ReservationStatus.CANCELLED_BY_STAFF)) {
            throw new IllegalStateException(
                    "This study space has been closed by staff for the selected date. " +
                            "Please choose another date or space."
            );
        }
    }


    /**
     * Throws if the user has reached the max number of active reservations for the day.
     */
    private void checkMaxReservationsPerDay(User user, LocalDate date, List<ReservationStatus> activeStatuses) {
        long activeCountForDay = reservationRepository.countByUserAndDateAndStatusIn(user, date, activeStatuses);
        if (activeCountForDay >= MAX_RESERVATIONS_PER_DAY) {
            throw new IllegalStateException(
                    "You have reached the maximum number of active reservations (" +
                            MAX_RESERVATIONS_PER_DAY + ") for this day."
            );
        }
    }


    /**
     * Throws if reservation is outside the study space's opening hours.
     */
    private void checkOpeningHours(StudySpace space, LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(space.getOpenTime()) || endTime.isAfter(space.getCloseTime())) {
            throw new IllegalStateException("Reservation time outside study space opening hours");
        }
    }


    /**
     * Throws if reservation duration exceeds the allowed maximum.
     */
    private void checkDurationWithinLimit(LocalTime startTime, LocalTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes > MAX_RESERVATION_DURATION_MINUTES) {
            throw new IllegalStateException("Maximum duration per reservation is 2 hours.");
        }
    }


    /**
     * Throws if the number of overlapping active reservations meets or exceeds the space's capacity.
     */
    private void checkCapacityForTimeRange(StudySpace space, LocalDate date, LocalTime startTime, LocalTime endTime) {
        long overlapping = reservationRepository.countOverlappingReservations(
                space,
                date,
                startTime,
                endTime,
                ACTIVE_RESERVATION_STATUSES
        );

        if (overlapping >= space.getCapacity()) {
            throw new IllegalStateException(
                    "No seats available for that time slot (" + startTime + " to " + endTime + " on " + date + ")."
            );
        }
    }


    /**
     * Throws if the user is currently penalized (blocked from making reservations).
     */
    private void checkUserNotPenalized(User user) {
        LocalDate penaltyUntil = user.getPenaltyUntil();
        LocalDate today = LocalDate.now();
        if (penaltyUntil != null && (penaltyUntil.isAfter(today) || penaltyUntil.isEqual(today))) {
            throw new IllegalStateException(
                    "You are blocked from making reservations until " + penaltyUntil + "."
            );
        }
    }


    /**
     * Persists a new confirmed reservation after all business rules have passed.
     */
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

    /**
     * Marks a reservation as no-show (user did not show up), applies a 3-day penalty to the user.
     * Only allowed for confirmed and past reservations.
     */
    public void markNoShow(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed reservations can be marked as no-show.");
        }

        if (!isNoShowEligible(reservation)) {
            throw new IllegalStateException("Cannot mark a future reservation as no-show.");
        }

        User user = reservation.getUser();

        // Impose a 3-day penalty
        user.setPenaltyUntil(LocalDate.now().plusDays(3));

        // Change reservation status
        reservation.setStatus(ReservationStatus.NO_SHOW);

        reservationRepository.save(reservation);
        userRepository.save(user);
    }

    /**
     * Returns true if the reservation is eligible to be marked as no-show (i.e., in the past).
     */
    private boolean isNoShowEligible(Reservation reservation) {
        LocalDate today = LocalDate.now();
        if (reservation.getDate().isBefore(today)) {
            return true;
        }
        if (reservation.getDate().isAfter(today)) {
            return false;
        }
        return reservation.getStartTime().isBefore(LocalTime.now());
    }
}
