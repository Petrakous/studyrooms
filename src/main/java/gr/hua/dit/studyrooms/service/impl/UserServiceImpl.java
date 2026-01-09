package gr.hua.dit.studyrooms.service.impl;

import gr.hua.dit.studyrooms.dto.UserRegistrationDto;
import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.entity.UserRole;
import gr.hua.dit.studyrooms.repository.UserRepository;
import gr.hua.dit.studyrooms.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


// Service implementation for user management (registration, lookup)
@Service
@Transactional
public class UserServiceImpl implements UserService {


    // Repository for User entity database operations
    private final UserRepository userRepository;
    // Password encoder for secure password storage
    private final PasswordEncoder passwordEncoder;


    // Constructor injection of dependencies
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * Registers a new student user.
     * Performs basic checks for unique username/email and matching passwords.
     * Encodes the password and assigns the STUDENT role.
     */
    @Override
    public User registerStudent(UserRegistrationDto dto) {
        // Basic checks – can be improved with validation later
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalStateException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalStateException("Passwords do not match");
        }

        // Create new User entity and set fields
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // Securely encode password
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setRole(UserRole.STUDENT); // Assign student role

        // Save user to database
        return userRepository.save(user);
    }


    /**
     * Find a user by username.
     * Returns an Optional<User>.
     */
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
