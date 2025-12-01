package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.dto.OccupancyStatsEntry;
import gr.hua.dit.studyrooms.entity.StudySpace;

import java.time.LocalDate;
import java.util.List;

public interface ReservationStatisticsService {

    List<OccupancyStatsEntry> getDailyOccupancy(StudySpace space, LocalDate startDate, LocalDate endDate);
}
