package gr.hua.dit.studyrooms.controller.api;

import gr.hua.dit.studyrooms.dto.ReservationFormDto;
import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.security.CustomUserDetails;
import gr.hua.dit.studyrooms.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationApiController {

    private final ReservationService reservationService;

    public ReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // GET /api/reservations/my
    @GetMapping("/my")
    public ResponseEntity<List<Reservation>> getMyReservations(Authentication auth) {
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        return ResponseEntity.ok(reservationService.getReservationsForUser(user));
    }

    // POST /api/reservations
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationFormDto form,
                                               Authentication auth) {
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        try {
            Reservation r = reservationService.createReservation(
                    user,
                    form.getStudySpaceId(),
                    form.getDate(),
                    form.getStartTime(),
                    form.getEndTime()
            );
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE /api/reservations/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelMyReservation(@PathVariable Long id,
                                                    Authentication auth) {
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        reservationService.cancelReservation(id, user);
        return ResponseEntity.noContent().build();
    }
}
