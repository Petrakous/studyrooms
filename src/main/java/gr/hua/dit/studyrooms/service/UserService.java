package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.dto.UserRegistrationDto;
import gr.hua.dit.studyrooms.entity.User;

import java.util.Optional;

public interface UserService {

    User registerStudent(UserRegistrationDto dto);

    Optional<User> findByUsername(String username);
}
