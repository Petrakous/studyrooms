
// Package declaration for the ReservationStatus enum
package gr.hua.dit.studyrooms.entity;

/**
 * Enum representing the possible statuses of a reservation in the study rooms system.
 * Each status describes a different stage or outcome of a reservation's lifecycle.
 */
public enum ReservationStatus {
    /**
     * The reservation has been created and is waiting for confirmation.
     */
    PENDING,

    /**
     * The reservation has been confirmed and is active.
     */
    CONFIRMED,

    /**
     * The reservation was cancelled by the user.
     */
    CANCELLED,

    /**
     * The reservation was cancelled by library staff.
     */
    CANCELLED_BY_STAFF,

    /**
     * The user did not show up for the reservation.
     */
    NO_SHOW
}
