
// Package declaration for the DTO (Data Transfer Object) class
package gr.hua.dit.studyrooms.dto;


/**
 * HomeStats is a simple Data Transfer Object (DTO) used to encapsulate
 * statistics for the home page, such as reservation and space availability data.
 */
public class HomeStats {


    // Number of upcoming reservations
    private long upcomingReservations;

    // Number of spaces currently available
    private long spacesAvailableNow;

    // Total number of reservations made today
    private long totalReservationsToday;


    /**
     * Constructs a HomeStats object with the provided statistics.
     *
     * @param upcomingReservations    Number of upcoming reservations
     * @param spacesAvailableNow      Number of spaces currently available
     * @param totalReservationsToday  Total reservations made today
     */
    public HomeStats(long upcomingReservations, long spacesAvailableNow, long totalReservationsToday) {
        this.upcomingReservations = upcomingReservations;
        this.spacesAvailableNow = spacesAvailableNow;
        this.totalReservationsToday = totalReservationsToday;
    }


    /**
     * Gets the number of upcoming reservations.
     * @return upcoming reservations count
     */
    public long getUpcomingReservations() {
        return upcomingReservations;
    }


    /**
     * Gets the number of spaces currently available.
     * @return available spaces count
     */
    public long getSpacesAvailableNow() {
        return spacesAvailableNow;
    }


    /**
     * Gets the total number of reservations made today.
     * @return total reservations today
     */
    public long getTotalReservationsToday() {
        return totalReservationsToday;
    }
}
