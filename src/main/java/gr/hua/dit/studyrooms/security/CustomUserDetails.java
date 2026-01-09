package gr.hua.dit.studyrooms.security;

import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Custom implementation of Spring Security's UserDetails interface
// Adapts the application's User entity for use with Spring Security
public class CustomUserDetails implements UserDetails {


    // The application's User entity being wrapped
    private final User user;


    // Constructor: initializes with a User entity
    public CustomUserDetails(User user) {
        this.user = user;
    }


    // Returns the wrapped User entity
    public User getUser() {
        return user;
    }


    // Returns the authorities granted to the user (user's role as a GrantedAuthority)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        UserRole role = user.getRole();
        String roleName = "ROLE_" + role.name(); // e.g., ROLE_STUDENT, ROLE_STAFF
        return List.of(new SimpleGrantedAuthority(roleName));
    }


    // Returns the user's password (for authentication)
    @Override
    public String getPassword() {
        return user.getPassword();
    }


    // Returns the user's username (for authentication)
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // The following methods always return true, meaning all accounts are considered active and valid
    // (for this assignment, no account restrictions are enforced)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
