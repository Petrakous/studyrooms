package gr.hua.dit.studyrooms.config;

import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.entity.UserRole;
import gr.hua.dit.studyrooms.repository.StudySpaceRepository;
import gr.hua.dit.studyrooms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudySpaceRepository studySpaceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudySpaceRepository studySpaceRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studySpaceRepository = studySpaceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // === Default USERS ===
        if (userRepository.count() == 0) {

            User staff = new User();
            staff.setUsername("staff");
            staff.setPassword(passwordEncoder.encode("staff123"));
            staff.setFullName("Staff User");
            staff.setEmail("staff@studyrooms.local");
            staff.setRole(UserRole.STAFF);
            userRepository.save(staff);

            User student = new User();
            student.setUsername("student");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setFullName("Student User");
            student.setEmail("student@studyrooms.local");
            student.setRole(UserRole.STUDENT);
            userRepository.save(student);
        }

        // === Default STUDY SPACES ===
        if (studySpaceRepository.count() == 0) {

            StudySpace space1 = new StudySpace();
            space1.setName("Library A - Quiet Zone");
            space1.setDescription("Quiet area suitable for individual study.");
            space1.setCapacity(10);
            space1.setOpenTime(LocalTime.of(8, 0));
            space1.setCloseTime(LocalTime.of(20, 0));
            studySpaceRepository.save(space1);

            StudySpace space2 = new StudySpace();
            space2.setName("Reading Room 1");
            space2.setDescription("Group study room with whiteboard.");
            space2.setCapacity(6);
            space2.setOpenTime(LocalTime.of(9, 0));
            space2.setCloseTime(LocalTime.of(22, 0));
            studySpaceRepository.save(space2);
        }
    }
}
