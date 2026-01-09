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

/**
 * ReservationController handles HTTP requests related to study room reservations.
 * Provides endpoints for both students (create/view/cancel reservations)
 * and staff (manage all reservations, mark no-show, close spaces).
 * 
 * Uses Spring Security for role-based access control:
 * - Students: Can manage their own reservations
 * - Staff: Can manage all reservations with @PreAuthorize annotations
 */
@Controller
public class ReservationController {

    // Injected services for business logic
    private final ReservationService reservationService;
    private final StudySpaceService studySpaceService;

    /**
     * Constructor with dependency injection for required services.
     * Spring automatically provides the services when creating this controller.
     */
    public ReservationController(ReservationService reservationService,
                                 StudySpaceService studySpaceService) {
        this.reservationService = reservationService;
        this.studySpaceService = studySpaceService;
    }

    // ========== STUDENT OPERATIONS ==========

    /**
     * Displays the reservation form for creating a new reservation.
     * Optionally pre-fills the form with a specific study space if spaceId parameter is provided.
     * 
     * @param spaceId Optional parameter to pre-select a study space
     * @param model Model to pass data to the view template
     * @return reservation_form.html template name
     */
    @GetMapping("/reservations/new")
    public String showReservationForm(@RequestParam(required = false) Long spaceId,
                                      Model model) {

        // Create a new empty form object
        ReservationFormDto form = new ReservationFormDto();
        
        // If a spaceId was provided, pre-fill it in the form
        if (spaceId != null) {
            form.setStudySpaceId(spaceId);
        }

        // Add form and all available spaces to the model for the view
        model.addAttribute("form", form);
        model.addAttribute("spaces", studySpaceService.getAllSpaces());

        return "reservation_form";
    }

    /**
     * Handles form submission for creating a new reservation.
     * Validates the form input and creates a reservation if valid.
     * If validation fails or service throws an exception, redisplays the form with errors.
     * 
     * @param form DTO containing reservation details (validated with @Valid)
     * @param bindingResult Contains validation errors if any
     * @param auth Spring Security authentication object containing current user info
     * @param model Model to pass data to the view
     * @return Redirects to "my reservations" page on success, or back to form on error
     */
    @PostMapping("/reservations")
    public String createReservation(@Valid @ModelAttribute("form") ReservationFormDto form,
                                    BindingResult bindingResult,
                                    Authentication auth,
                                    Model model) {

        // Check if form validation failed (e.g., missing required fields)
        if (bindingResult.hasErrors()) {
            // Reload spaces dropdown and show form with validation errors
            model.addAttribute("spaces", studySpaceService.getAllSpaces());
            return "reservation_form";
        }

        // Extract the current authenticated user from Spring Security
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        try {
            // Attempt to create the reservation with user and form data
            reservationService.createReservation(
                    user,
                    form.getStudySpaceId(),
                    form.getDate(),
                    form.getStartTime(),
                    form.getEndTime()
            );
            // Success: Redirect to user's reservations page
            return "redirect:/reservations/my";

        } catch (Exception e) {
            bindingResult.reject("reservationError", e.getMessage());
            model.addAttribute("spaces", studySpaceService.getAllSpaces());
            return "reservation_form";
        }
    }

    /**
     * Displays the list of reservations for the currently authenticated student.
     * Shows past and future reservations that the student has made.
     * 
     * @param auth Spring Security authentication containing current user
     * @param model Model to pass reservations to the view
     * @return reservations_my.html template name
     */
    @GetMapping("/reservations/my")
    public String myReservations(Authentication auth, Model model) {
        // Extract the current authenticated student
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        // Fetch all reservations for this user
        List<Reservation> reservations = reservationService.getReservationsForUser(user);
        
        // Pass reservations to the view template
        model.addAttribute("reservations", reservations);
        return "reservations_my";
    }

    /**
     * Cancels a reservation made by the current student.
     * Validates that the student owns the reservation before allowing cancellation.
     * 
     * @param id The reservation ID to cancel
     * @param auth Spring Security authentication containing current user
     * @param redirectAttributes Used to pass flash attributes (error messages) to redirect page
     * @return Redirects back to "my reservations" page
     */
    @PostMapping("/reservations/{id}/cancel")
    public String cancelMyReservation(@PathVariable Long id,
                                      Authentication auth,
                                      RedirectAttributes redirectAttributes) {
        // Extract the current authenticated student
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        try {
            // Attempt to cancel the reservation (service validates ownership)
            reservationService.cancelReservation(id, user);
        } catch (RuntimeException ex) {
            // If cancellation fails, add error message as flash attribute to show on redirect
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/reservations/my";
    }

    // ========== STAFF OPERATIONS ==========

    /**
     * Displays all reservations for a specific date.
     * Staff can view and manage all student reservations for any given date.
     * Requires STAFF role via Spring Security.
     * 
     * @param date Optional date parameter (defaults to today if not provided)
     * @param model Model to pass reservations and spaces to the view
     * @return staff_reservations.html template name
     */
    @GetMapping("/staff/reservations")
    @PreAuthorize("hasRole('STAFF')")
    public String staffReservations(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Model model) {

        // If no date provided, default to today
        LocalDate selectedDate = (date != null) ? date : LocalDate.now();

        // Fetch all reservations for the selected date
        List<Reservation> reservations =
                reservationService.getReservationsForDate(selectedDate);

        // Add all spaces for the "Close space" dropdown functionality
        model.addAttribute("spaces", studySpaceService.getAllSpaces());

        // Add data needed by the view template
        model.addAttribute("reservations", reservations);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("nowTime", java.time.LocalTime.now());

        return "staff_reservations";
    }

    /**
     * Cancels a reservation as staff (bypasses ownership checks).
     * Staff can cancel any reservation regardless of who created it.
     * Requires STAFF role.
     * 
     * @param id The reservation ID to cancel
     * @param date Optional date to redirect back to (defaults to today)
     * @return Redirects back to staff reservations page for the specified date
     */
    @PostMapping("/staff/reservations/{id}/cancel")
    @PreAuthorize("hasRole('STAFF')")
    public String staffCancelReservation(@PathVariable Long id,
                                         @RequestParam(value = "date", required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                         LocalDate date) {

        // Cancel the reservation without ownership validation
        reservationService.cancelReservationAsStaff(id);

        // Redirect back to staff reservations page (use provided date or default to today)
        LocalDate redirectDate = (date != null) ? date : LocalDate.now();
        return "redirect:/staff/reservations?date=" + redirectDate;
    }

    /**
     * Marks a reservation as "no-show" when the student doesn't arrive.
     * This affects student's booking history/reputation.
     * Requires STAFF role.
     * 
     * @param id The reservation ID to mark as no-show
     * @param date Optional date to redirect back to
     * @param redirectAttributes Used to pass error messages if marking fails
     * @return Redirects back to staff reservations page
     */
    @PostMapping("/staff/reservations/{id}/no-show")
    @PreAuthorize("hasRole('STAFF')")
    public String staffMarkNoShow(@PathVariable Long id,
                                  @RequestParam(value = "date", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                  LocalDate date,
                                  RedirectAttributes redirectAttributes) {

        try {
            // Mark the reservation as a no-show in the system
            reservationService.markNoShow(id);
        } catch (RuntimeException ex) {
            // If marking fails, pass error message to show on redirected page
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        // Redirect back to reservations page for the specified date
        LocalDate redirectDate = (date != null) ? date : LocalDate.now();
        return "redirect:/staff/reservations?date=" + redirectDate;
    }

    /**
     * Closes an entire study space for a specific date.
     * This cancels all reservations for that space on that day.
     * Useful for maintenance, cleaning, or administrative closures.
     * Requires STAFF role.
     * 
     * @param spaceId The ID of the study space to close
     * @param date The date to close the space for
     * @param redirectAttributes Used to pass summary message about cancelled reservations
     * @return Redirects back to staff reservations page with success message
     */
    @PostMapping("/staff/close-space")
    @PreAuthorize("hasRole('STAFF')")
    public String closeSpaceForDay(@RequestParam("spaceId") Long spaceId,
                                   @RequestParam("date")
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                   LocalDate date,
                                   RedirectAttributes redirectAttributes) {

        // Cancel all reservations for this space and date, returns count of cancelled reservations
        int cancelled = reservationService.cancelByStaffForSpaceAndDate(spaceId, date);

        // Create and add a summary message showing how many reservations were cancelled
        redirectAttributes.addFlashAttribute(
                "message",
                "Space closed for " + date + ". Cancelled " + cancelled + " reservation(s)."
        );

        // Redirect back to reservations page for the specified date
        return "redirect:/staff/reservations?date=" + date;
    }
}
