package gr.hua.dit.studyrooms.controller.api;

import gr.hua.dit.studyrooms.dto.UserRegistrationDto;
import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import gr.hua.dit.studyrooms.dto.LoginRequestDto;
import gr.hua.dit.studyrooms.dto.LoginResponseDto;
import gr.hua.dit.studyrooms.security.CustomUserDetails;
import gr.hua.dit.studyrooms.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * REST API Controller for handling user authentication and registration.
 * This controller provides endpoints for user login and registration.
 * All endpoints are mapped to /api/auth base path.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication and registration endpoints")
public class AuthApiController {

    // Service for managing user-related operations
    private final UserService userService;
    
    // Spring Security component for authenticating credentials
    private final AuthenticationManager authenticationManager;
    
    // Service for generating and validating JWT tokens
    private final JwtService jwtService;

    /**
     * Constructor for dependency injection.
     * 
     * @param userService for user registration and management
     * @param authenticationManager for credential validation
     * @param jwtService for JWT token generation
     */
    public AuthApiController(UserService userService,
                             AuthenticationManager authenticationManager,
                             JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Register a new student account in the system.
     * 
     * Endpoint: POST /api/auth/register
     * @param dto User registration data (username, password, email, etc.)
     * @return ResponseEntity containing the new username on success, or error message on failure
     */
    @Operation(summary = "Register a new student account")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto dto) {
        try {
            // Validate and create a new student user
            User user = userService.registerStudent(dto);
            // Return the registered username upon successful registration
            return ResponseEntity.ok(user.getUsername());
        } catch (Exception e) {
            // Return a 400 Bad Request with the error message if registration fails
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Authenticate a user with username and password credentials.
     * Upon successful authentication, returns a JWT token for API access.
     * 
     * Endpoint: POST /api/auth/login
     * @param request Login credentials (username and password)
     * @return ResponseEntity containing JWT token on success, or 401 Unauthorized with error message on failure
     */
    @Operation(summary = "Authenticate with username and password to receive a JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            // Attempt to authenticate the user using the provided credentials
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Extract user details from the authenticated principal
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

            // Generate a JWT token for the authenticated user
            String token = jwtService.generateToken(userDetails);

            // Return the token wrapped in a LoginResponseDto
            return ResponseEntity.ok(new LoginResponseDto(token));
        } catch (Exception e) {
            // Return 401 Unauthorized if authentication fails (invalid credentials)
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

}
