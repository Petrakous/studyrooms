package gr.hua.dit.studyrooms.entity;

public enum ReservationStatus {
    PENDING,    // δημιουργήθηκε, περιμένει
    CONFIRMED,  // επιβεβαιωμένη
    CANCELLED,   // ακυρωμένη
    CANCELLED_BY_STAFF //Ακυρωμένη απο προσωπικό βιβλιοθήκης
}
