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


// Repository interface for Reservation entity, providing custom query methods for reservation management
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Find all reservations for a specific user ("My reservations").
    List<Reservation> findByUser(User user);

    // Find all reservations for a specific study space on a given date.
    List<Reservation> findByStudySpaceAndDate(StudySpace studySpace, LocalDate date);

    // Find all reservations for a study space between two dates (inclusive).
    List<Reservation> findByStudySpaceAndDateBetween(StudySpace studySpace, LocalDate startDate, LocalDate endDate);

    // Find all reservations for a specific date (e.g., for staff view).
    List<Reservation> findByDate(LocalDate date);


    // Find reservations for a study space on a date with specific statuses.
    List<Reservation> findByStudySpaceAndDateAndStatusIn(
            StudySpace studySpace,
            LocalDate date,
            List<ReservationStatus> statuses
    );

    // Count total reservations for a specific date.
    long countByDate(LocalDate date);

    /**
     * Count upcoming reservations for a user (future or today with start time >= now).
     * Uses a JPQL query to count reservations where the date is after today,
     * * or the date is today and the start time is in the future.
     */
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

    // Count active reservations for a user on a specific date with given statuses.
    long countByUserAndDateAndStatusIn(
            User user,
            LocalDate date,
            Collection<ReservationStatus> statuses
    );

    // Check if a reservation exists for a study space, date, and status.
    boolean existsByStudySpaceAndDateAndStatus(StudySpace studySpace,
                                               LocalDate date,
                                               ReservationStatus status);

    /**
     * Count reservations that overlap with a given time range for a space, date, and statuses.
     * Uses a JPQL query to check for time overlaps.
     */
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

    // Count reservations for a study space, date, and a set of statuses.
    long countByStudySpaceAndDateAndStatusIn(StudySpace space,
                                             LocalDate date,
                                             Collection<ReservationStatus> statuses
    );
           
    void deleteByStudySpaceId(Long studySpaceId);

    void deleteByDemoTrue();
}
