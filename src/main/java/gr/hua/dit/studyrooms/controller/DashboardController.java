package gr.hua.dit.studyrooms.controller;

import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;              // <--- ΠΡΟΣΘΗΚΗ
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {  // <--- ΠΡΟΣΘΗΚΗ Model
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        if (user.getPenaltyUntil() != null &&
                !user.getPenaltyUntil().isBefore(LocalDate.now())) {
            model.addAttribute("penaltyUntil", user.getPenaltyUntil());
        }

        return "dashboard";
    }
}
