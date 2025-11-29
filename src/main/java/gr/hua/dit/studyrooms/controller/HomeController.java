package gr.hua.dit.studyrooms.controller;

import gr.hua.dit.studyrooms.dto.HomeStats;
import gr.hua.dit.studyrooms.dto.HomeStatsService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final HomeStatsService homeStatsService;

    public HomeController(HomeStatsService homeStatsService) {
        this.homeStatsService = homeStatsService;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            HomeStats stats = homeStatsService.getStatsForUser(username);
            model.addAttribute("stats", stats);
        }

        return "home";
    }
}