package gr.hua.dit.studyrooms.controller;

import gr.hua.dit.studyrooms.dto.StudySpaceDto;
import gr.hua.dit.studyrooms.dto.StudySpaceMapper;
import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.service.StudySpaceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import gr.hua.dit.studyrooms.availability.TimeSlotAvailability;
import gr.hua.dit.studyrooms.availability.SpaceAvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * Main Spring MVC Controller for managing study spaces.
 * Handles requests for both students (viewing spaces) and staff (managing spaces).
 * All staff operations are protected with role-based access control (@PreAuthorize).
 */
@Controller
public class StudySpaceController {

    // Services for business logic and availability management
    private final StudySpaceService studySpaceService;
    private final SpaceAvailabilityService spaceAvailabilityService;

    /**
     * Constructor for dependency injection of required services.
     * @param studySpaceService Service for CRUD operations on study spaces
     * @param spaceAvailabilityService Service for managing time slot availability
     */
    public StudySpaceController(StudySpaceService studySpaceService,
                                SpaceAvailabilityService spaceAvailabilityService) {
        this.studySpaceService = studySpaceService;
        this.spaceAvailabilityService = spaceAvailabilityService;
    }

    // ========== STUDENT VIEW - PUBLIC ENDPOINTS ==========

    @GetMapping("/spaces")
    public String listSpaces(Model model) {
        model.addAttribute("spaces", studySpaceService.getAllSpaces());
        return "spaces";
    }

    @GetMapping("/spaces/{id}")
    public String spaceDetails(@PathVariable Long id,
                               @RequestParam(value = "date", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                               Model model) {

        StudySpace space = studySpaceService.getSpaceById(id);

        LocalDate selectedDate = (date != null) ? date : LocalDate.now();

        List<TimeSlotAvailability> slots =
                spaceAvailabilityService.getDailyAvailability(space, selectedDate);

        model.addAttribute("space", space);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("slots", slots);

        return "space_details";
    }

    // ---------- STAFF VIEW ----------

    @GetMapping("/staff/spaces")
    @PreAuthorize("hasAnyRole('STAFF')")
    public String manageSpaces(Model model) {
        model.addAttribute("spaces", studySpaceService.getAllSpaces());
        return "manage_spaces";
    }

    @GetMapping("/staff/spaces/new")
    @PreAuthorize("hasAnyRole('STAFF')")
    public String newSpaceForm(Model model) {
        model.addAttribute("space", new StudySpaceDto());
        return "space_form";
    }

    @PostMapping("/staff/spaces")
    @PreAuthorize("hasAnyRole('STAFF')")
    public String createSpace(@Valid @ModelAttribute("space") StudySpaceDto space,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "space_form";
        }

        studySpaceService.createSpace(StudySpaceMapper.toEntity(space));
        return "redirect:/staff/spaces";
    }

    @GetMapping("/staff/spaces/{id}/edit")
    @PreAuthorize("hasAnyRole('STAFF')")
    public String editSpace(@PathVariable Long id, Model model) {
        model.addAttribute("space", StudySpaceMapper.toDto(studySpaceService.getSpaceById(id)));
        return "space_form";
    }

    @PostMapping("/staff/spaces/{id}")
    @PreAuthorize("hasAnyRole('STAFF')")
    public String updateSpace(@PathVariable Long id,
                              @Valid @ModelAttribute("space") StudySpaceDto space,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "space_form";
        }

        studySpaceService.updateSpace(id, StudySpaceMapper.toEntity(space));
        return "redirect:/staff/spaces";
    }

    @PostMapping("/staff/spaces/{id}/delete")
    @PreAuthorize("hasAnyRole('STAFF')")
    public String deleteSpace(@PathVariable Long id) {
        studySpaceService.deleteSpace(id);
        return "redirect:/staff/spaces";
    }

}
