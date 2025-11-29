package gr.hua.dit.studyrooms.dto;

public class HomeStats {

    private long upcomingReservations;
    private long spacesAvailableNow;
    private long totalReservationsToday;

    public HomeStats(long upcomingReservations, long spacesAvailableNow, long totalReservationsToday) {
        this.upcomingReservations = upcomingReservations;
        this.spacesAvailableNow = spacesAvailableNow;
        this.totalReservationsToday = totalReservationsToday;
    }

    public long getUpcomingReservations() {
        return upcomingReservations;
    }

    public long getSpacesAvailableNow() {
        return spacesAvailableNow;
    }

    public long getTotalReservationsToday() {
        return totalReservationsToday;
    }
}
