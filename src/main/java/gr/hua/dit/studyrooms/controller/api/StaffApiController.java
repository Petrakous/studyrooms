package gr.hua.dit.studyrooms.controller.api;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.service.ReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('STAFF')")
public class StaffApiController {

    private final ReservationService reservationService;

    public StaffApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // GET /api/staff/reservations
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
    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservationAsStaff(id);
        return ResponseEntity.noContent().build();
    }
}
