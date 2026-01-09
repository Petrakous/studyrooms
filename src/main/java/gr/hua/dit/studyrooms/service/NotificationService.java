
// Service interface for sending notifications related to reservations
package gr.hua.dit.studyrooms.service;


import gr.hua.dit.studyrooms.entity.Reservation;


/**
 * Interface for notification services related to reservation events.
 */
public interface NotificationService {


    /**
     * Notify when a new reservation is created.
     * @param reservation The reservation that was created.
     */
    void notifyReservationCreated(Reservation reservation);


    /**
     * Notify when a reservation is cancelled.
     * @param reservation The reservation that was cancelled.
     * @param cancelledByStaff True if cancelled by staff, false if by user.
     */
    void notifyReservationCancelled(Reservation reservation, boolean cancelledByStaff);
}
