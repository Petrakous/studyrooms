package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


/**
 * Service interface for managing study room reservations.
 * Provides methods for creating, retrieving, canceling, and updating reservations.
 */
public interface ReservationService {


    /**
     * Retrieves all reservations for a specific user.
     * @param user the user whose reservations to fetch
     * @return list of reservations for the user
     */
    List<Reservation> getReservationsForUser(User user);


    /**
     * Retrieves all reservations for a specific date.
     * @param date the date to filter reservations
     * @return list of reservations on the given date
     */
    List<Reservation> getReservationsForDate(LocalDate date);


    /**
     * Retrieves all reservations in the system.
     * @return list of all reservations
     */
    List<Reservation> getAll();


    /**
     * Creates a new reservation for a user in a specific study space.
     * @param user the user making the reservation
     * @param studySpaceId the ID of the study space
     * @param date the date of the reservation
     * @param startTime the start time of the reservation
     * @param endTime the end time of the reservation
     * @return the created Reservation object
     */
    Reservation createReservation(User user, Long studySpaceId,
                                  LocalDate date, LocalTime startTime, LocalTime endTime);


    /**
     * Cancels a reservation for a user.
     * @param reservationId the ID of the reservation to cancel
     * @param user the user requesting the cancellation
     */
    void cancelReservation(Long reservationId, User user);


    /**
     * Cancels a reservation as a staff member (no user check).
     * @param reservationId the ID of the reservation to cancel
     */
    void cancelReservationAsStaff(Long reservationId);


    /**
     * Cancels all reservations for a specific space and date as staff.
     * @param spaceId the ID of the study space
     * @param date the date for which to cancel reservations
     * @return the number of reservations canceled
     */
    int cancelByStaffForSpaceAndDate(Long spaceId, LocalDate date);


    /**
     * Marks a reservation as a no-show.
     * @param reservationId the ID of the reservation to mark
     */
    void markNoShow(Long reservationId);
}
