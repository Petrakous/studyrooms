package gr.hua.dit.studyrooms.dto;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.repository.ReservationRepository;
import gr.hua.dit.studyrooms.repository.StudySpaceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class HomeStatsService {

    private final ReservationRepository reservationRepository;
    private final StudySpaceRepository studySpaceRepository;

    public HomeStatsService(ReservationRepository reservationRepository,
                            StudySpaceRepository studySpaceRepository) {
        this.reservationRepository = reservationRepository;
        this.studySpaceRepository = studySpaceRepository;
    }

    public HomeStats getStatsForUser(String username) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 1) Upcoming reservations for this user (από σήμερα και μετά)
        long upcomingForUser =
                reservationRepository.countUpcomingForUser(username, today, now);


        // 2) Spaces available now (απλά: ανοιχτοί χώροι αυτή την ώρα)
        long spacesOpenNow =
                studySpaceRepository.countByOpenTimeLessThanEqualAndCloseTimeGreaterThan(now, now);

        // 3) Total reservations today (όλων των χρηστών)
        long totalToday =
                reservationRepository.countByDate(today);

        return new HomeStats(upcomingForUser, spacesOpenNow, totalToday);
    }
}
