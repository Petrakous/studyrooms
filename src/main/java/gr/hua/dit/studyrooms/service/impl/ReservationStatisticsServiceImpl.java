package gr.hua.dit.studyrooms.service.impl;

import gr.hua.dit.studyrooms.dto.OccupancyStatsEntry;
import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.ReservationStatus;
import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.repository.ReservationRepository;
import gr.hua.dit.studyrooms.service.ReservationStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReservationStatisticsServiceImpl implements ReservationStatisticsService {

    private static final EnumSet<ReservationStatus> ACTIVE_STATUSES = EnumSet.of(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED
    );

    private final ReservationRepository reservationRepository;

    public ReservationStatisticsServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<OccupancyStatsEntry> getDailyOccupancy(StudySpace space, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        List<Reservation> reservations = reservationRepository.findByStudySpaceAndDateBetween(space, startDate, endDate);
        Map<LocalDate, List<Reservation>> reservationsByDate = reservations.stream()
                .filter(r -> ACTIVE_STATUSES.contains(r.getStatus()))
                .collect(Collectors.groupingBy(Reservation::getDate));

        long totalMinutes = Math.max(0, Duration.between(space.getOpenTime(), space.getCloseTime()).toMinutes());

        List<OccupancyStatsEntry> results = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            List<Reservation> dayReservations = reservationsByDate.getOrDefault(cursor, List.of());
            long occupiedMinutes = dayReservations.stream()
                    .mapToLong(r -> Duration.between(r.getStartTime(), r.getEndTime()).toMinutes())
                    .sum();

            results.add(new OccupancyStatsEntry(cursor, dayReservations.size(), occupiedMinutes, totalMinutes));
            cursor = cursor.plusDays(1);
        }

        return results;
    }
}
