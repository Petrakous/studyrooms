package gr.hua.dit.studyrooms.controller;

import gr.hua.dit.studyrooms.dto.HomeStats;
import gr.hua.dit.studyrooms.dto.HomeStatsService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller class responsible for handling requests to the home page.
 * Manages user authentication and provides home statistics to the view.
 */
@Controller
public class HomeController {

    // Service dependency for retrieving user statistics
    private final HomeStatsService homeStatsService;

    /**
     * Constructor for dependency injection of HomeStatsService.
     * @param homeStatsService service for fetching user-specific statistics
     */
    public HomeController(HomeStatsService homeStatsService) {
        this.homeStatsService = homeStatsService;
    }

    /**
     * Handles GET requests to the home page root path ("/").
     * If a user is authenticated, retrieves their statistics and makes them available to the view.
     * 
     * @param authentication Spring Security Authentication object containing user info
     * @param model Model object to pass data to the view template
     * @return the name of the view template to render ("home")
     */
    @GetMapping("/")
    public String home(Authentication authentication, Model model) {

        // Check if a user is authenticated and valid
        if (authentication != null && authentication.isAuthenticated()) {
            // Retrieve the authenticated user's username
            String username = authentication.getName();
            // Fetch statistics for the authenticated user from the service
            HomeStats stats = homeStatsService.getStatsForUser(username);
            // Add the stats object to the model so the view template can access it
            model.addAttribute("stats", stats);
        }

        // Return the view template name to be rendered by Spring
        return "home";
    }
}