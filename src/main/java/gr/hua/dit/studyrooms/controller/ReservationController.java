package gr.hua.dit.studyrooms.controller;

import gr.hua.dit.studyrooms.dto.ReservationFormDto;
import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.security.CustomUserDetails;
import gr.hua.dit.studyrooms.service.ReservationService;
import gr.hua.dit.studyrooms.service.StudySpaceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ReservationController {

    private final ReservationService reservationService;
    private final StudySpaceService studySpaceService;

    public ReservationController(ReservationService reservationService,
                                 StudySpaceService studySpaceService) {
        this.reservationService = reservationService;
        this.studySpaceService = studySpaceService;
    }

    // ---------- STUDENT ----------

    // φόρμα νέας κράτησης
    @GetMapping("/reservations/new")
    public String showReservationForm(@RequestParam(required = false) Long spaceId,
                                      Model model) {

        ReservationFormDto form = new ReservationFormDto();
        if (spaceId != null) {
            form.setStudySpaceId(spaceId);
        }

        model.addAttribute("form", form);
        model.addAttribute("spaces", studySpaceService.getAllSpaces());

        return "reservation_form";
    }

    @PostMapping("/reservations")
    public String createReservation(@Valid @ModelAttribute("form") ReservationFormDto form,
                                    BindingResult bindingResult,
                                    Authentication auth,
                                    Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("spaces", studySpaceService.getAllSpaces());
            return "reservation_form";
        }

        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        try {
            reservationService.createReservation(
                    user,
                    form.getStudySpaceId(),
                    form.getDate(),
                    form.getStartTime(),
                    form.getEndTime()
            );
            return "redirect:/reservations/my";

        } catch (Exception e) {
            bindingResult.reject("reservationError", e.getMessage());
            model.addAttribute("spaces", studySpaceService.getAllSpaces());
            return "reservation_form";
        }
    }

    // "My reservations"
    @GetMapping("/reservations/my")
    public String myReservations(Authentication auth, Model model) {
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        List<Reservation> reservations = reservationService.getReservationsForUser(user);
        model.addAttribute("reservations", reservations);
        return "reservations_my";
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancelMyReservation(@PathVariable Long id,
                                      Authentication auth,
                                      RedirectAttributes redirectAttributes) {
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        try {
            reservationService.cancelReservation(id, user);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/reservations/my";
    }

    // ---------- STAFF ----------

    @GetMapping("/staff/reservations")
    @PreAuthorize("hasRole('STAFF')")
    public String staffReservations(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Model model) {

        // αν δεν δοθεί date -> σήμερα
        LocalDate selectedDate = (date != null) ? date : LocalDate.now();

        // κρατήσεις της ημέρας
        List<Reservation> reservations =
                reservationService.getReservationsForDate(selectedDate);

        // όλα τα spaces για το dropdown "Close space"
        model.addAttribute("spaces", studySpaceService.getAllSpaces());

        model.addAttribute("reservations", reservations);
        model.addAttribute("selectedDate", selectedDate);

        return "staff_reservations";
    }

    @PostMapping("/staff/reservations/{id}/cancel")
    @PreAuthorize("hasRole('STAFF')")
    public String staffCancelReservation(@PathVariable Long id,
                                         @RequestParam(value = "date", required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                         LocalDate date) {

        reservationService.cancelReservationAsStaff(id);

        // αν δεν ήρθε date από το hidden field, γύρνα στη σημερινή μέρα
        LocalDate redirectDate = (date != null) ? date : LocalDate.now();
        return "redirect:/staff/reservations?date=" + redirectDate;
    }

    @PostMapping("/staff/reservations/{id}/no-show")
    @PreAuthorize("hasRole('STAFF')")
    public String staffMarkNoShow(@PathVariable Long id,
                                  @RequestParam(value = "date", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                  LocalDate date) {

        reservationService.markNoShow(id);

        LocalDate redirectDate = (date != null) ? date : LocalDate.now();
        return "redirect:/staff/reservations?date=" + redirectDate;
    }

    @PostMapping("/staff/close-space")
    @PreAuthorize("hasRole('STAFF')")
    public String closeSpaceForDay(@RequestParam("spaceId") Long spaceId,
                                   @RequestParam("date")
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                   LocalDate date,
                                   RedirectAttributes redirectAttributes) {

        int cancelled = reservationService.cancelByStaffForSpaceAndDate(spaceId, date);

        redirectAttributes.addFlashAttribute(
                "message",
                "Space closed for " + date + ". Cancelled " + cancelled + " reservation(s)."
        );

        return "redirect:/staff/reservations?date=" + date;
    }
}
