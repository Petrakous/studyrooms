package gr.hua.dit.studyrooms.controller;

import gr.hua.dit.studyrooms.dto.UserRegistrationDto;
import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController handles authentication-related operations including user login and registration.
 * It provides endpoints for displaying login/registration pages and processing registration submissions.
 * After successful registration, users are automatically authenticated and logged in.
 */
@Controller
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    /**
     * Constructor that injects dependencies for user service and authentication.
     * 
     * @param userService handles user-related business logic (registration, etc.)
     * @param authenticationManager Spring Security component that authenticates users
     */
    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Displays the login page.
     * Maps to GET /login endpoint.
     * 
     * @return the name of the login template to render
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Displays the user registration form.
     * Maps to GET /register endpoint.
     * Initializes an empty UserRegistrationDto object for form binding.
     * 
     * @param model the Spring model to add attributes to
     * @return the name of the register template to render
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    /**
     * Processes user registration form submission.
     * Maps to POST /register endpoint.
     * 
     * Workflow:
     * 1. Validates the registration data using @Valid annotation
     * 2. If validation fails, redisplays the form with error messages
     * 3. If validation succeeds, registers the user via UserService
     * 4. Automatically authenticates the new user (auto-login functionality)
     * 5. Redirects to dashboard on success, or back to registration form on error
     * 
     * @param dto the user registration data transfer object with validation annotations
     * @param bindingResult contains validation results
     * @param model the Spring model for adding error messages
     * @return redirect to dashboard on success, or the register template with errors on failure
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto dto,
                               BindingResult bindingResult,
                               Model model) {
        // Check if there are validation errors (empty fields, invalid format, etc.)
        if (bindingResult.hasErrors()) {
            // Redisplay the registration form with validation error messages
            return "register";
        }

        try {
            // Register the new student user in the database
            User saved = userService.registerStudent(dto);

            // Auto-login feature: Automatically authenticate the user after registration
            // Create authentication token with username and password
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            saved.getUsername(),
                            dto.getPassword()  // password from registration form
                    )
            );
            
            // Store the authentication in the security context
            // This makes the user "logged in" without needing to login manually
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Redirect to dashboard after successful registration and auto-login
            return "redirect:/dashboard";
        } catch (Exception e) {
            // Handle any exceptions during registration (e.g., duplicate username, database errors)
            model.addAttribute("error", e.getMessage());
            // Redisplay the registration form with error message
            return "register";
        }
    }
}
