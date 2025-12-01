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

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation operations for authenticated users")
@SecurityRequirement(name = "bearerAuth")
public class ReservationApiController {

    private final ReservationService reservationService;

    public ReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // GET /api/reservations/my
    @Operation(summary = "List reservations for the authenticated user")
    @GetMapping("/my")
    public ResponseEntity<List<Reservation>> getMyReservations(Authentication auth) {
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        return ResponseEntity.ok(reservationService.getReservationsForUser(user));
    }

    // POST /api/reservations
    @Operation(summary = "Create a reservation for the authenticated user")
    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody ReservationFormDto form,
                                               Authentication auth) {
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        Reservation r = reservationService.createReservation(
                user,
                form.getStudySpaceId(),
                form.getDate(),
                form.getStartTime(),
                form.getEndTime()
        );
        return ResponseEntity.ok(r);
    }

    // DELETE /api/reservations/{id}
    @Operation(summary = "Cancel one of the authenticated user's reservations")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelMyReservation(@PathVariable Long id,
                                                    Authentication auth) {
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        reservationService.cancelReservation(id, user);
        return ResponseEntity.noContent().build();
    }
}
