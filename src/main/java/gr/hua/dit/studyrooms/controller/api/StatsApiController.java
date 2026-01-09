package gr.hua.dit.studyrooms.controller.api;

import gr.hua.dit.studyrooms.dto.OccupancyStatsEntry;
import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.service.ReservationStatisticsService;
import gr.hua.dit.studyrooms.service.StudySpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST API controller for statistics endpoints.
 * Provides occupancy and reservation statistics for study spaces.
 * All endpoints require STAFF role authorization and bearer token authentication.
 */
@RestController
@RequestMapping("/api/stats")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Statistics", description = "Staff-only statistics endpoints")
public class StatsApiController {

    // Service for calculating and retrieving reservation statistics
    private final ReservationStatisticsService reservationStatisticsService;
    // Service for fetching study space information
    private final StudySpaceService studySpaceService;

    /**
     * Constructor for dependency injection of services.
     * Uses Spring's constructor-based injection for immutable dependencies.
     * 
     * @param reservationStatisticsService Service for statistics calculations
     * @param studySpaceService Service for space data retrieval
     */
    public StatsApiController(ReservationStatisticsService reservationStatisticsService,
                              StudySpaceService studySpaceService) {
        this.reservationStatisticsService = reservationStatisticsService;
        this.studySpaceService = studySpaceService;
    }

    /**
     * Retrieves occupancy statistics for a specific study space.
     * 
     * Endpoint: GET /api/stats/occupancy
     * Required Role: STAFF
     * Authentication: Bearer token required
     * 
     * This endpoint calculates and returns daily occupancy metrics between two dates
     * for a particular study space. Useful for tracking usage patterns and resource utilization.
     * 
     * @param spaceId The ID of the study space to fetch occupancy statistics for
     * @param startDate The start date for the statistics period (format: yyyy-MM-dd)
     * @param endDate The end date for the statistics period (format: yyyy-MM-dd)
     * @return ResponseEntity containing a list of OccupancyStatsEntry objects with daily metrics
     */
    @Operation(summary = "Daily occupancy for a space", description = "Returns occupancy metrics between the given dates.")
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/occupancy")
    public ResponseEntity<List<OccupancyStatsEntry>> occupancy(
            @RequestParam("spaceId") Long spaceId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Fetch the study space entity by ID to validate existence and get space details
        StudySpace space = studySpaceService.getSpaceById(spaceId);
        
        // Calculate daily occupancy statistics for the specified period
        List<OccupancyStatsEntry> stats = reservationStatisticsService.getDailyOccupancy(space, startDate, endDate);
        
        // Return the statistics as a successful HTTP 200 response
        return ResponseEntity.ok(stats);
    }
}
