
// Service interface for providing reservation statistics, such as occupancy data for study spaces.
package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.dto.OccupancyStatsEntry;
import gr.hua.dit.studyrooms.entity.StudySpace;

import java.time.LocalDate;
import java.util.List;


/**
 * Service interface for retrieving reservation statistics, specifically occupancy statistics for study spaces.
 */
public interface ReservationStatisticsService {

    /**
     * Returns a list of daily occupancy statistics for a given study space within a date range.
     *
     * @param space the StudySpace for which to retrieve occupancy statistics
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return a list of OccupancyStatsEntry objects representing daily occupancy
     */
    List<OccupancyStatsEntry> getDailyOccupancy(StudySpace space, LocalDate startDate, LocalDate endDate);
}
