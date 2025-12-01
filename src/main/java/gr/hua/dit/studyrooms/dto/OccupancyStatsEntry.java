package gr.hua.dit.studyrooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Occupancy metrics for a specific date")
public class OccupancyStatsEntry {

    private final LocalDate date;
    private final long reservationsCount;
    private final long occupiedMinutes;
    private final long totalMinutes;

    public OccupancyStatsEntry(LocalDate date, long reservationsCount, long occupiedMinutes, long totalMinutes) {
        this.date = date;
        this.reservationsCount = reservationsCount;
        this.occupiedMinutes = occupiedMinutes;
        this.totalMinutes = totalMinutes;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getReservationsCount() {
        return reservationsCount;
    }

    public long getOccupiedMinutes() {
        return occupiedMinutes;
    }

    public long getTotalMinutes() {
        return totalMinutes;
    }

    @Schema(description = "Occupancy percentage for the day")
    public double getOccupancyPercentage() {
        if (totalMinutes <= 0) {
            return 0;
        }
        return (double) occupiedMinutes * 100 / totalMinutes;
    }
}
