package gr.hua.dit.studyrooms.entity;

public enum ReservationStatus {
    CONFIRMED,  // επιβεβαιωμένη
    CANCELLED,   // ακυρωμένη
    CANCELLED_BY_STAFF, // ακυρωμένη απο προσωπικό βιβλιοθήκης
    NO_SHOW // Δεν εμφανίστηκε στην κράτηση
}
