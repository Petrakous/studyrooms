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

    // Only consider reservations with these statuses as "active" for statistics
    private static final EnumSet<ReservationStatus> ACTIVE_STATUSES = EnumSet.of(
            ReservationStatus.CONFIRMED
    );

    // Repository for accessing reservation data
    private final ReservationRepository reservationRepository;

    // Constructor injection of the reservation repository
    public ReservationStatisticsServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Calculates daily occupancy statistics for a given study space and date range.
     * For each day, computes the number of reservations, total occupied minutes (capped),
     * and the total available seat-minutes for the space.
     */
    @Override
    public List<OccupancyStatsEntry> getDailyOccupancy(StudySpace space, LocalDate startDate, LocalDate endDate) {
        // Validate date range
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        // Fetch all reservations for the space in the date range
        List<Reservation> reservations = reservationRepository.findByStudySpaceAndDateBetween(space, startDate, endDate);

        // Group reservations by date, filtering only those with active statuses
        Map<LocalDate, List<Reservation>> reservationsByDate = reservations.stream()
                .filter(r -> ACTIVE_STATUSES.contains(r.getStatus()))
                .collect(Collectors.groupingBy(Reservation::getDate));

        long totalMinutes = space.isFullDay()
                ? Duration.ofHours(23).plusMinutes(59).toMinutes()
                : Math.max(0, Duration.between(space.getOpenTime(), space.getCloseTime()).toMinutes());
        long totalSeatMinutes = totalMinutes * Math.max(1, space.getCapacity());

        List<OccupancyStatsEntry> results = new ArrayList<>();
        LocalDate cursor = startDate;
        // Iterate through each day in the range
        while (!cursor.isAfter(endDate)) {
            // Get reservations for the current day (or empty list)
            List<Reservation> dayReservations = reservationsByDate.getOrDefault(cursor, List.of());
            // Sum the occupied minutes for all reservations on this day
            long occupiedMinutes = dayReservations.stream()
                    .mapToLong(r -> Duration.between(r.getStartTime(), r.getEndTime()).toMinutes())
                    .sum();

            // Cap the occupied minutes at the total available seat-minutes
            long cappedOccupiedMinutes = Math.min(occupiedMinutes, totalSeatMinutes);
            // Add the statistics entry for this day
            results.add(new OccupancyStatsEntry(cursor, dayReservations.size(), cappedOccupiedMinutes, totalSeatMinutes));
            cursor = cursor.plusDays(1);
        }

        return results;
    }
}
