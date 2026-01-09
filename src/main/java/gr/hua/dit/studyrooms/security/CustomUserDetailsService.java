
// Package declaration
package gr.hua.dit.studyrooms.security;


import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


// Marks this class as a Spring service component
@Service
public class CustomUserDetailsService implements UserDetailsService {


    // Repository for accessing User data from the database
    private final UserRepository userRepository;


    // Constructor injection of UserRepository
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

        /**
         * Loads the user by username for authentication.
         * @param username the username identifying the user whose data is required
         * @return UserDetails object containing user information
         * @throws UsernameNotFoundException if the user is not found
         */
        @Override
        public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Attempt to find the user by username in the database
        User user = userRepository.findByUsername(username)
            .orElseThrow(() ->
                // Throw exception if user is not found
                new UsernameNotFoundException("User not found: " + username));

        // Wrap the User entity in a CustomUserDetails object for Spring Security
        return new CustomUserDetails(user);
        }
}
