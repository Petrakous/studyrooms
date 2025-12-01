package gr.hua.dit.studyrooms.repository;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.ReservationStatus;
import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // "My reservations"
    List<Reservation> findByUser(User user);

    // κρατήσεις για έναν χώρο σε συγκεκριμένη μέρα
    List<Reservation> findByStudySpaceAndDate(StudySpace studySpace, LocalDate date);

    List<Reservation> findByStudySpaceAndDateBetween(StudySpace studySpace, LocalDate startDate, LocalDate endDate);

    // Όλες οι κρατήσεις μιας ημέρας (π.χ. για staff view)
    List<Reservation> findByDate(LocalDate date);

    List<Reservation> findByStudySpaceAndDateAndStatusIn(
            StudySpace studySpace,
            LocalDate date,
            List<ReservationStatus> statuses
    );

    // πόσες κρατήσεις υπάρχουν σήμερα συνολικά
    long countByDate(LocalDate date);

    // πόσες επερχόμενες κρατήσεις έχει ένας χρήστης συνολικά
    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.user.username = :username
          AND (
                r.date > :today
                OR (r.date = :today AND r.startTime >= :nowTime)
              )
        """)
    long countUpcomingForUser(
            @Param("username") String username,
            @Param("today") LocalDate today,
            @Param("nowTime") LocalTime nowTime
    );

    // Χρησιμοποιείται για τον κανόνα: μέχρι Χ ενεργές κρατήσεις ανά ημέρα
    long countByUserAndDateAndStatusIn(
            User user,
            LocalDate date,
            Collection<ReservationStatus> statuses
    );

    boolean existsByStudySpaceAndDateAndStatus(StudySpace studySpace,
                                               LocalDate date,
                                               ReservationStatus status);

    @Query("""
    SELECT COUNT(r) FROM Reservation r
    WHERE r.studySpace = :space
      AND r.date = :date
      AND r.status IN :statuses
      AND r.startTime < :endTime
      AND r.endTime > :startTime
    """)
    long countOverlappingReservations(
            @Param("space") StudySpace space,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<ReservationStatus> statuses
    );

    long countByStudySpaceAndDateAndStatusIn(StudySpace space,
                                             LocalDate date,
                                             Collection<ReservationStatus> statuses);

    }
