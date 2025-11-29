package gr.hua.dit.studyrooms.controller;

import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        // Αν θέλεις μπορείς να κάνεις cast:
        // CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        // User user = cud.getUser();
        return "dashboard";
    }
}
