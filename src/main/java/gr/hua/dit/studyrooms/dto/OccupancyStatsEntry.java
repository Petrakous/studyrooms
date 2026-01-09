
// DTO for representing occupancy statistics for a specific date
package gr.hua.dit.studyrooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Occupancy metrics for a specific date")
/**
 * Data Transfer Object (DTO) representing occupancy statistics for a specific date.
 * Contains reservation count, occupied minutes, total available minutes, and occupancy percentage.
 */
public class OccupancyStatsEntry {

    // The date for which the statistics are recorded
    private final LocalDate date;
    // Number of reservations made for the date
    private final long reservationsCount;
    // Total minutes rooms were occupied on the date
    private final long occupiedMinutes;
    // Total minutes available for occupancy on the date
    private final long totalMinutes;

    /**
     * Constructor to initialize all fields.
     * @param date The date for the statistics
     * @param reservationsCount Number of reservations
     * @param occupiedMinutes Total occupied minutes
     * @param totalMinutes Total available minutes
     */
    public OccupancyStatsEntry(LocalDate date, long reservationsCount, long occupiedMinutes, long totalMinutes) {
        this.date = date;
        this.reservationsCount = reservationsCount;
        this.occupiedMinutes = occupiedMinutes;
        this.totalMinutes = totalMinutes;
    }

    /**
     * @return The date for the statistics
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return Number of reservations for the date
     */
    public long getReservationsCount() {
        return reservationsCount;
    }

    /**
     * @return Total minutes rooms were occupied
     */
    public long getOccupiedMinutes() {
        return occupiedMinutes;
    }

    /**
     * @return Total minutes available for occupancy
     */
    public long getTotalMinutes() {
        return totalMinutes;
    }

    /**
     * Calculates the occupancy percentage for the day.
     * @return Percentage of occupied minutes out of total available minutes (0 if totalMinutes <= 0)
     */
    @Schema(description = "Occupancy percentage for the day")
    public double getOccupancyPercentage() {
        if (totalMinutes <= 0) {
            return 0;
        }
        return (double) occupiedMinutes * 100 / totalMinutes;
    }
}
