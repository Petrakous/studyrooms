package gr.hua.dit.studyrooms.controller.api;

import gr.hua.dit.studyrooms.dto.ReservationFormDto;
import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.security.CustomUserDetails;
import gr.hua.dit.studyrooms.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for managing study room reservations.
 * 
 * Provides endpoints for authenticated users to:
 * - View their reservations
 * - Create new reservations
 * - Cancel existing reservations
 * 
 * All endpoints require Bearer token authentication via Spring Security.
 */
@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation operations for authenticated users")
@SecurityRequirement(name = "bearerAuth")
public class ReservationApiController {

    // Service layer dependency for reservation business logic
    private final ReservationService reservationService;

    /**
     * Constructor for dependency injection of ReservationService.
     * 
     * @param reservationService The service handling reservation operations
     */
    public ReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Retrieves all reservations for the authenticated user.
     * 
     * Endpoint: GET /api/reservations/my
     * 
     * @param auth Spring Security Authentication object containing the authenticated user
     * @return ResponseEntity with a list of the user's reservations
     */
    @Operation(summary = "List reservations for the authenticated user")
    @GetMapping("/my")
    public ResponseEntity<List<Reservation>> getMyReservations(Authentication auth) {
        // Extract the authenticated user from the security context
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        // Fetch all reservations for the user from the database
        return ResponseEntity.ok(reservationService.getReservationsForUser(user));
    }

    /**
     * Creates a new reservation for the authenticated user.
     * 
     * Endpoint: POST /api/reservations
     * 
     * @param form Validated ReservationFormDto containing reservation details
     *             (studySpaceId, date, startTime, endTime)
     * @param auth Spring Security Authentication object containing the authenticated user
     * @return ResponseEntity with the created Reservation object
     */
    @Operation(summary = "Create a reservation for the authenticated user")
    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody ReservationFormDto form,
                                               Authentication auth) {
        // Extract the authenticated user from the security context
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        // Create the reservation with user-provided details
        Reservation r = reservationService.createReservation(
                user,
                form.getStudySpaceId(),
                form.getDate(),
                form.getStartTime(),
                form.getEndTime()
        );
        return ResponseEntity.ok(r);
    }

    /**
     * Cancels a reservation for the authenticated user.
     * 
     * Endpoint: DELETE /api/reservations/{id}
     * 
     * @param id The ID of the reservation to cancel
     * @param auth Spring Security Authentication object containing the authenticated user
     * @return ResponseEntity with no content (204 status) on successful cancellation
     */
    @Operation(summary = "Cancel one of the authenticated user's reservations")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelMyReservation(@PathVariable Long id,
                                                    Authentication auth) {
        // Extract the authenticated user from the security context
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        // Cancel the reservation (authorization check ensures user can only cancel their own reservations)
        reservationService.cancelReservation(id, user);
        return ResponseEntity.noContent().build();
    }
}
