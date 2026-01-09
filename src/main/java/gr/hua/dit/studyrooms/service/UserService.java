package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.dto.UserRegistrationDto;
import gr.hua.dit.studyrooms.entity.User;

import java.util.Optional;


/**
 * Service interface for user-related operations, such as registration and lookup.
 * Typically implemented by a class that handles business logic for users.
 */
public interface UserService {


    /**
     * Registers a new student using the provided registration data transfer object.
     * @param dto Data transfer object containing student registration information
     * @return The registered User entity
     */
    User registerStudent(UserRegistrationDto dto);


    /**
     * Finds a user by their username.
     * @param username The username to search for
     * @return An Optional containing the User if found, or empty if not found
     */
    Optional<User> findByUsername(String username);
}
