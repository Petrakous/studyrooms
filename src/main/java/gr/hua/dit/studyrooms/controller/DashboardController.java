package gr.hua.dit.studyrooms.controller;

import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

/**
 * Controller responsible for handling dashboard-related requests.
 * Manages user dashboard display with penalty information.
 */
@Controller
public class DashboardController {

    /**
     * Handles GET requests to the /dashboard endpoint.
     * Retrieves the currently authenticated user and checks for active penalties.
     * If a penalty is active (penalty date is today or in the future), it adds
     * the penalty date to the model so the view can display it to the user.
     *
     * @param auth the Spring Security Authentication object containing user information
     * @param model the Model object used to pass data from controller to view
     * @return the name of the dashboard template to render ("dashboard")
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        // Extract CustomUserDetails from the Authentication principal
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        
        // Retrieve the User entity from CustomUserDetails
        User user = cud.getUser();

        // Check if user has an active penalty
        // Condition 1: penaltyUntil is not null (penalty exists)
        // Condition 2: penaltyUntil is not before today (penalty is still active)
        if (user.getPenaltyUntil() != null &&
                !user.getPenaltyUntil().isBefore(LocalDate.now())) {
            // Add the penalty end date to the model for the view to display
            model.addAttribute("penaltyUntil", user.getPenaltyUntil());
        }

        // Return the dashboard view template
        return "dashboard";
    }
}
