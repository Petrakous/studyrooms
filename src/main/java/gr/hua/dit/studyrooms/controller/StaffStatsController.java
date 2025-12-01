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

@Controller
@RequestMapping("/staff/occupancy")
@PreAuthorize("hasRole('STAFF')")
public class StaffStatsController {

    private final StudySpaceService studySpaceService;
    private final ReservationStatisticsService reservationStatisticsService;

    public StaffStatsController(StudySpaceService studySpaceService,
                                ReservationStatisticsService reservationStatisticsService) {
        this.studySpaceService = studySpaceService;
        this.reservationStatisticsService = reservationStatisticsService;
    }

    @GetMapping
    public String viewOccupancy(
            @RequestParam(value = "spaceId", required = false) Long spaceId,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        LocalDate effectiveStart = startDate != null ? startDate : LocalDate.now();
        LocalDate effectiveEnd = endDate != null ? endDate : effectiveStart.plusDays(6);

        model.addAttribute("spaces", studySpaceService.getAllSpaces());
        model.addAttribute("startDate", effectiveStart);
        model.addAttribute("endDate", effectiveEnd);

        if (spaceId != null) {
            try {
                StudySpace space = studySpaceService.getSpaceById(spaceId);
                model.addAttribute("selectedSpace", space);
                model.addAttribute("stats", reservationStatisticsService.getDailyOccupancy(space, effectiveStart, effectiveEnd));
            } catch (RuntimeException ex) {
                model.addAttribute("error", ex.getMessage());
            }
        }

        return "staff_occupancy";
    }
}
