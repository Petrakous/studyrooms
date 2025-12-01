package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationService {

    List<Reservation> getReservationsForUser(User user);

    List<Reservation> getReservationsForDate(LocalDate date);

    List<Reservation> getAll();

    Reservation createReservation(User user, Long studySpaceId,
                                  LocalDate date, LocalTime startTime, LocalTime endTime);

    void cancelReservation(Long reservationId, User user);

    void cancelReservationAsStaff(Long reservationId);

    int cancelByStaffForSpaceAndDate(Long spaceId, LocalDate date);

    void markNoShow(Long reservationId);
}
