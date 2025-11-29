package gr.hua.dit.studyrooms.config;

import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.entity.UserRole;
import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.ReservationStatus;
import gr.hua.dit.studyrooms.repository.StudySpaceRepository;
import gr.hua.dit.studyrooms.repository.UserRepository;
import gr.hua.dit.studyrooms.repository.ReservationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudySpaceRepository studySpaceRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReservationRepository reservationRepository;

    public DataInitializer(UserRepository userRepository,
                           StudySpaceRepository studySpaceRepository,
                           ReservationRepository reservationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studySpaceRepository = studySpaceRepository;
        this.reservationRepository = reservationRepository;
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

        // === Sample RESERVATIONS for student ===

        // φέρνουμε τον student από τη βάση (αν δεν υπάρχει, δεν κάνουμε τίποτα)
        User student = userRepository.findByUsername("student").orElse(null);

        // παίρνουμε όλα τα StudySpaces για να διαλέξουμε δωμάτια
        List<StudySpace> spaces = studySpaceRepository.findAll();

        if (student != null && !spaces.isEmpty() && reservationRepository.count() == 0) {

            StudySpace roomA = spaces.get(0);
            StudySpace roomB = spaces.size() > 1 ? spaces.get(1) : roomA;

            Reservation r1 = new Reservation();
            r1.setUser(student);
            r1.setStudySpace(roomA);
            r1.setDate(LocalDate.now().plusDays(1));
            r1.setStartTime(LocalTime.of(10, 0));
            r1.setEndTime(LocalTime.of(12, 0));
            r1.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(r1);

            Reservation r2 = new Reservation();
            r2.setUser(student);
            r2.setStudySpace(roomB);
            r2.setDate(LocalDate.now().plusDays(2));
            r2.setStartTime(LocalTime.of(14, 0));
            r2.setEndTime(LocalTime.of(15, 0));
            r2.setStatus(ReservationStatus.PENDING);
            reservationRepository.save(r2);

            Reservation r3 = new Reservation();
            r3.setUser(student);
            r3.setStudySpace(roomA);
            r3.setDate(LocalDate.now().minusDays(1));
            r3.setStartTime(LocalTime.of(18, 0));
            r3.setEndTime(LocalTime.of(20, 0));
            r3.setStatus(ReservationStatus.CANCELLED_BY_STAFF);
            reservationRepository.save(r3);
        }
    }
}
