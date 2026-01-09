package gr.hua.dit.studyrooms.dto;

import gr.hua.dit.studyrooms.repository.ReservationRepository;
import gr.hua.dit.studyrooms.repository.StudySpaceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;


// Service class providing statistics for the home page
@Service
public class HomeStatsService {


        // Repository for reservation data
        private final ReservationRepository reservationRepository;
        // Repository for study space data
        private final StudySpaceRepository studySpaceRepository;


        // Constructor with dependency injection for repositories
        public HomeStatsService(ReservationRepository reservationRepository,
                                                        StudySpaceRepository studySpaceRepository) {
                this.reservationRepository = reservationRepository;
                this.studySpaceRepository = studySpaceRepository;
        }

    /**
     * Returns statistics for the home page for a specific user.
     *
     * @param username the username to get stats for
     * @return HomeStats object containing:
     *   - upcoming reservations for the user
     *   - number of spaces currently open
     *   - total reservations today (all users)
     */
    public HomeStats getStatsForUser(String username) {
        // Get today's date and current time
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 1) Count upcoming reservations for this user (from today and now onwards)
        long upcomingForUser =
                reservationRepository.countUpcomingForUser(username, today, now);

        // 2) Count spaces that are currently open (openTime <= now < closeTime)
        long spacesOpenNow =
                studySpaceRepository.countOpenNow(now);

        // 3) Count total reservations made today (all users)
        long totalToday =
                reservationRepository.countByDate(today);

        // Return the statistics as a HomeStats object
        return new HomeStats(upcomingForUser, spacesOpenNow, totalToday);
    }
}
