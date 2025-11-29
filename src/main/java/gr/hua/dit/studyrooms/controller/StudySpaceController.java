package gr.hua.dit.studyrooms.controller;

import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.service.StudySpaceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import gr.hua.dit.studyrooms.availability.SpaceAvailabilityService;
import gr.hua.dit.studyrooms.availability.TimeSlotView;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Controller
public class StudySpaceController {

    private final StudySpaceService studySpaceService;
    private final SpaceAvailabilityService spaceAvailabilityService;

    public StudySpaceController(StudySpaceService studySpaceService,
                                SpaceAvailabilityService spaceAvailabilityService) {
        this.studySpaceService = studySpaceService;
        this.spaceAvailabilityService = spaceAvailabilityService;
    }

    // ---------- STUDENT VIEW ----------

    @GetMapping("/spaces")
    public String listSpaces(Model model) {
        model.addAttribute("spaces", studySpaceService.getAllSpaces());
        return "spaces";
    }

    @GetMapping("/spaces/{id}")
    public String spaceDetails(@PathVariable Long id,
                               @RequestParam(value = "date", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                               LocalDate date,
                               Model model) {

        StudySpace space = studySpaceService.getSpaceById(id);

        LocalDate selectedDate = (date != null) ? date : LocalDate.now();

        List<TimeSlotView> slots =
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
        model.addAttribute("space", new StudySpace());
        return "space_form";
    }

    @PostMapping("/staff/spaces")
    @PreAuthorize("hasAnyRole('STAFF')")
    public String createSpace(@Valid @ModelAttribute("space") StudySpace space,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "space_form";
        }

        studySpaceService.createSpace(space);
        return "redirect:/staff/spaces";
    }

    @GetMapping("/staff/spaces/{id}/edit")
    @PreAuthorize("hasAnyRole('STAFF')")
    public String editSpace(@PathVariable Long id, Model model) {
        model.addAttribute("space", studySpaceService.getSpaceById(id));
        return "space_form";
    }

    @PostMapping("/staff/spaces/{id}")
    @PreAuthorize("hasAnyRole('STAFF')")
    public String updateSpace(@PathVariable Long id,
                              @Valid @ModelAttribute("space") StudySpace space,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            space.setId(id);
            return "space_form";
        }

        studySpaceService.updateSpace(id, space);
        return "redirect:/staff/spaces";
    }

    @PostMapping("/staff/spaces/{id}/delete")
    @PreAuthorize("hasAnyRole('STAFF')")
    public String deleteSpace(@PathVariable Long id) {
        studySpaceService.deleteSpace(id);
        return "redirect:/staff/spaces";
    }
}
