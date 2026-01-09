package gr.hua.dit.studyrooms.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


// Main security configuration class for the application
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


        // Custom JWT authentication filter for API requests
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        // Inject the JWT authentication filter via constructor
        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }


        // Bean for encoding passwords using BCrypt
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }


        // Bean for authentication manager, used for authenticating users
        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }


    /**
     * Security filter chain for API endpoints (stateless, JWT-based)
     * Applies to all /api/** routes
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Only apply this filter chain to /api/** endpoints
                .securityMatcher("/api/**")
                // Disable CSRF for APIs (stateless)
                .csrf(AbstractHttpConfigurer::disable)
                // Use stateless session management (no HTTP session)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configure which API endpoints are public and which require authentication
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/weather",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll() // Allow unauthenticated access to these endpoints
                        .anyRequest().authenticated() // All other API requests require authentication
                )
                // Disable form login and logout for APIs
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                // Add JWT authentication filter before the default username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    /**
     * Security filter chain for MVC (web) endpoints
     * Applies to all non-API routes (web pages, static resources, etc.)
     */
    @Bean
    @Order(2)
    public SecurityFilterChain mvcSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CSRF except for H2 console
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                // Configure which web endpoints are public, restricted, or require authentication
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/home",
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/spa/**",
                                "/images/**",
                                "/h2-console/**",
                                "/spaces",
                                "/spaces/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll() // Allow unauthenticated access to these endpoints
                        .requestMatchers("/staff/**").hasAnyRole("STAFF") // Restrict /staff/** to STAFF role
                        .anyRequest().authenticated() // All other requests require authentication
                )
                // Use session if required (for web logins)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // Configure custom login page and default success URL
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/dashboard", true)
                )
                // Configure logout URL and redirect
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                // Handle access denied exceptions
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))
                // Allow frames for H2 console
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
