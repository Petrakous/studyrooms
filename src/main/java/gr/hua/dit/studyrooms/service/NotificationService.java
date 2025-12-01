package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.entity.Reservation;

public interface NotificationService {

    void notifyReservationCreated(Reservation reservation);

    void notifyReservationCancelled(Reservation reservation, boolean cancelledByStaff);
}
