package gr.hua.dit.studyrooms.controller;

import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.service.ReservationStatisticsService;
import gr.hua.dit.studyrooms.service.StudySpaceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Controller for handling staff occupancy statistics and analytics.
 * 
 * This controller manages the staff occupancy view, allowing staff members to view
 * reservation statistics and occupancy rates for study spaces over a given date range.
 * Access is restricted to users with the STAFF role.
 */
@Controller
@RequestMapping("/staff/occupancy")
@PreAuthorize("hasRole('STAFF')")
public class StaffStatsController {

    // Service for managing study space operations
    private final StudySpaceService studySpaceService;
    
    // Service for calculating and retrieving reservation statistics
    private final ReservationStatisticsService reservationStatisticsService;

    /**
     * Constructor using dependency injection to initialize required services.
     * 
     * @param studySpaceService the service for space operations
     * @param reservationStatisticsService the service for statistics calculations
     */
    public StaffStatsController(StudySpaceService studySpaceService,
                                ReservationStatisticsService reservationStatisticsService) {
        this.studySpaceService = studySpaceService;
        this.reservationStatisticsService = reservationStatisticsService;
    }

    /**
     * Displays occupancy statistics for study spaces within a specified date range.
     * 
     * If a specific space is selected, displays detailed occupancy data for that space.
     * If no date range is provided, defaults to the current date through the next 6 days.
     * 
     * @param spaceId optional ID of the space to view details for
     * @param startDate optional start date for the statistics period (ISO format)
     * @param endDate optional end date for the statistics period (ISO format)
     * @param model the model to pass data to the view
     * @return the name of the template to render (staff_occupancy)
     */
    @GetMapping
    public String viewOccupancy(
            @RequestParam(value = "spaceId", required = false) Long spaceId,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        // Set effective start date: use provided date or default to today
        LocalDate effectiveStart = startDate != null ? startDate : LocalDate.now();
        
        // Set effective end date: use provided date or default to 6 days after start date
        LocalDate effectiveEnd = endDate != null ? endDate : effectiveStart.plusDays(6);

        // Add all available spaces to the model for the dropdown/selection in the view
        model.addAttribute("spaces", studySpaceService.getAllSpaces());
        
        // Add the date range to the model for display in the view
        model.addAttribute("startDate", effectiveStart);
        model.addAttribute("endDate", effectiveEnd);

        // If a specific space is selected, fetch and add detailed occupancy statistics
        if (spaceId != null) {
            try {
                // Retrieve the selected study space by ID
                StudySpace space = studySpaceService.getSpaceById(spaceId);
                model.addAttribute("selectedSpace", space);
                
                // Calculate and retrieve daily occupancy statistics for the space within the date range
                model.addAttribute("stats", reservationStatisticsService.getDailyOccupancy(space, effectiveStart, effectiveEnd));
            } catch (RuntimeException ex) {
                // If space lookup fails, add error message to the model for display
                model.addAttribute("error", ex.getMessage());
            }
        }

        // Return the staff_occupancy template to render the view
        return "staff_occupancy";
    }
}
