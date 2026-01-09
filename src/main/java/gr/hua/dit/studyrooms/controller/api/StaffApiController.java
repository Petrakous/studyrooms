package gr.hua.dit.studyrooms.controller.api;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST API Controller for staff-only reservation management operations.
 * 
 * Provides endpoints for staff members to:
 * - View all reservations or filter by specific date
 * - Cancel any reservation (without ownership restrictions)
 * 
 * All endpoints require:
 * - Bearer token authentication via Spring Security
 * - User to have the STAFF role
 */
@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('STAFF')")
@Tag(name = "Staff", description = "Staff-only reservation management")
@SecurityRequirement(name = "bearerAuth")
public class StaffApiController {

    // Service layer dependency for reservation business logic
    private final ReservationService reservationService;

    /**
     * Constructor for dependency injection of ReservationService.
     * 
     * @param reservationService The service handling reservation operations
     */
    public StaffApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Retrieves reservations for staff view, optionally filtered by date.
     * 
     * Endpoint: GET /api/staff/reservations
     * Query Parameters:
     *   - date (optional): Filter reservations by specific date in ISO format (YYYY-MM-DD)
     * 
     * @param date Optional date parameter to filter reservations for a specific date
     * @return ResponseEntity with a list of all reservations or reservations for the specified date
     */
    @Operation(summary = "List reservations for a specific date or all")
    @GetMapping("/reservations")
    public ResponseEntity<List<Reservation>> getReservations(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        // Fetch reservations based on whether a date filter was provided
        List<Reservation> reservations;
        if (date != null) {
            // If a date is provided, retrieve only reservations for that specific date
            reservations = reservationService.getReservationsForDate(date);
        } else {
            // If no date is provided, retrieve all reservations in the system
            reservations = reservationService.getAll();
        }

        return ResponseEntity.ok(reservations);
    }

    /**
     * Cancels a reservation with staff privileges.
     * 
     * Endpoint: POST /api/staff/reservations/{id}/cancel
     * 
     * Unlike regular users who can only cancel their own reservations,
     * staff members can cancel any reservation in the system.
     * 
     * @param id The ID of the reservation to cancel
     * @return ResponseEntity with no content (204 status) on successful cancellation
     */
    @Operation(summary = "Cancel a reservation as staff")
    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        // Cancel the reservation with staff-level privileges (no user ownership check)
        reservationService.cancelReservationAsStaff(id);
        return ResponseEntity.noContent().build();
    }
}
