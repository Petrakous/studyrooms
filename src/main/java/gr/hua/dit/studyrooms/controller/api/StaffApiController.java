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

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('STAFF')")
@Tag(name = "Staff", description = "Staff-only reservation management")
@SecurityRequirement(name = "bearerAuth")
public class StaffApiController {

    private final ReservationService reservationService;

    public StaffApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // GET /api/staff/reservations
    @Operation(summary = "List reservations for a specific date or all")
    @GetMapping("/reservations")
    public ResponseEntity<List<Reservation>> getReservations(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        List<Reservation> reservations;
        if (date != null) {
            reservations = reservationService.getReservationsForDate(date);
        } else {
            reservations = reservationService.getAll();
        }

        return ResponseEntity.ok(reservations);
    }

    // POST /api/staff/reservations/{id}/cancel
    @Operation(summary = "Cancel a reservation as staff")
    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservationAsStaff(id);
        return ResponseEntity.noContent().build();
    }
}
