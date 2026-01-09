package gr.hua.dit.studyrooms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


// This filter intercepts HTTP requests to handle JWT-based authentication.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    // Service for JWT operations (extracting username, validating token, etc.)
    private final JwtService jwtService;
    // Loads user-specific data
    private final UserDetailsService userDetailsService;


    // Constructor injection of dependencies
    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Get the Authorization header from the request
        String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        // Check if the header is present and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract the JWT token from the header
            jwt = authHeader.substring(7);
            try {
                // Extract the username from the JWT token
                username = jwtService.extractUsername(jwt);
            } catch (Exception e) {
                // If the token is invalid, ignore and continue without authentication
            }
        }

        // If a username was extracted and the user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user details from the database or another source
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate the JWT token against the user details
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Create an authentication token for the user
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                // Set additional authentication details from the request
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue the filter chain (proceed with the request)
        filterChain.doFilter(request, response);
    }
}
