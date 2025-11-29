package gr.hua.dit.studyrooms.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // encoder για τα passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // χρειάζεται αν αργότερα κάνουμε manual authentication (π.χ. JWT)
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CSRF: το κρατάμε ενεργό, αλλά αγνοούμε κάποια paths (h2, api)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**")
                )

                // δικαιώματα πρόσβασης
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home",
                                "/login", "/register",
                                "/css/**", "/js/**", "/images/**",
                                "/h2-console/**",
                                "/spaces", "/spaces/**"
                        ).permitAll()

                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

                        .requestMatchers("/staff/**").hasAnyRole("STAFF")

                        // προσωρινά: τα /api/** τα αφήνουμε authenticated με session.
                        // Αργότερα θα βάλουμε JWT.
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // φόρμα login
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        // μετά το login, πού πάει ο χρήστης
                        .defaultSuccessUrl("/dashboard", true)
                )

                // logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )

                // 👉👉 ΕΔΩ προσθέτουμε το Access Denied page
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                )

                // για να δουλεύει το H2 console (frames)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
