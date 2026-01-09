package gr.hua.dit.studyrooms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling error pages in the Study Rooms application.
 * Provides endpoints to display custom error pages to users.
 */
@Controller
public class ErrorPagesController {

    /**
     * Handles requests to the access-denied page.
     * This endpoint is typically triggered when a user attempts to access
     * a resource they don't have permission to view.
     *
     * @return the name of the view template "access-denied" to be rendered
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
