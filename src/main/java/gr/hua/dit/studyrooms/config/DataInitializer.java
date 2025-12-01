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
import java.util.Random;

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

            // δεύτερος φοιτητής για πιο ρεαλιστικά demo
            User student2 = new User();
            student2.setUsername("student2");
            student2.setPassword(passwordEncoder.encode("student123"));
            student2.setFullName("Second Student");
            student2.setEmail("student2@studyrooms.local");
            student2.setRole(UserRole.STUDENT);
            userRepository.save(student2);
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

            StudySpace space3 = new StudySpace();
            space3.setName("Computer Lab");
            space3.setDescription("PC lab with workstations and printer.");
            space3.setCapacity(20);
            space3.setOpenTime(LocalTime.of(10, 0));
            space3.setCloseTime(LocalTime.of(18, 0));
            studySpaceRepository.save(space3);
        }

        // === Sample RESERVATIONS ===

        User student = userRepository.findByUsername("student").orElse(null);
        User student2 = userRepository.findByUsername("student2").orElse(null);
        List<StudySpace> spaces = studySpaceRepository.findAll();

        // Για να μην γεμίζουμε διπλές, μόνο όταν η DB είναι άδεια από reservations
        if (student != null && !spaces.isEmpty() && reservationRepository.count() == 0) {

            StudySpace roomA = spaces.get(0);
            StudySpace roomB = spaces.size() > 1 ? spaces.get(1) : roomA;
            StudySpace roomC = spaces.size() > 2 ? spaces.get(2) : roomA;

            // --- Υπάρχοντα demo (κρατάμε όπως είναι) ---

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

            // --- Extra σταθερά demo για όλες τις καταστάσεις ---

            // CANCELLED στο παρελθόν
            Reservation r4 = new Reservation();
            r4.setUser(student);
            r4.setStudySpace(roomB);
            r4.setDate(LocalDate.now().minusDays(2));
            r4.setStartTime(LocalTime.of(9, 0));
            r4.setEndTime(LocalTime.of(11, 0));
            r4.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(r4);

            // NO_SHOW στο παρελθόν (για να βλέπεις penalty cases κτλ)
            if (student2 != null) {
                Reservation r5 = new Reservation();
                r5.setUser(student2);
                r5.setStudySpace(roomA);
                r5.setDate(LocalDate.now().minusDays(3));
                r5.setStartTime(LocalTime.of(12, 0));
                r5.setEndTime(LocalTime.of(14, 0));
                r5.setStatus(ReservationStatus.NO_SHOW);
                reservationRepository.save(r5);
            }

            // Μια κρατηση σήμερα, confirmed
            Reservation r6 = new Reservation();
            r6.setUser(student);
            r6.setStudySpace(roomC);
            r6.setDate(LocalDate.now());
            r6.setStartTime(LocalTime.of(16, 0));
            r6.setEndTime(LocalTime.of(18, 0));
            r6.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(r6);

            // --- Random demo data: διαφορετικά κάθε run (σε φρέσκια DB) ---

            Random random = new Random();

            // 10 random reservations για student / student2 σε διάφορους χώρους & μέρες
            for (int i = 0; i < 10; i++) {

                User randomUser = (i % 2 == 0 || student2 == null) ? student : student2;
                StudySpace randomSpace = spaces.get(random.nextInt(spaces.size()));

                // random μέρα από 3 μέρες πριν μέχρι 7 μέρες μετά
                int dayOffset = random.nextInt(11) - 3; // -3..+7
                LocalDate date = LocalDate.now().plusDays(dayOffset);

                // χρονικό διάστημα 1 ή 2 ωρών μέσα στο ωράριο του χώρου
                LocalTime open = randomSpace.getOpenTime();
                LocalTime close = randomSpace.getCloseTime();

                // να έχουμε χώρο τουλάχιστον 2 ώρες για πιο safe επιλογή
                int availableHours = Math.max(1, close.getHour() - open.getHour() - 1);
                int startOffsetHours = availableHours > 0 ? random.nextInt(availableHours) : 0;

                LocalTime start = open.plusHours(startOffsetHours);
                LocalTime end = start.plusHours(random.nextBoolean() ? 1 : 2);
                if (end.isAfter(close)) {
                    end = close;
                }

                // random status
                ReservationStatus status;
                int roll = random.nextInt(5);
                switch (roll) {
                    case 0 -> status = ReservationStatus.CONFIRMED;
                    case 1 -> status = ReservationStatus.PENDING;
                    case 2 -> status = ReservationStatus.CANCELLED;
                    case 3 -> status = ReservationStatus.CANCELLED_BY_STAFF;
                    default -> status = ReservationStatus.NO_SHOW;
                }

                Reservation demo = new Reservation();
                demo.setUser(randomUser);
                demo.setStudySpace(randomSpace);
                demo.setDate(date);
                demo.setStartTime(start);
                demo.setEndTime(end);
                demo.setStatus(status);

                reservationRepository.save(demo);
            }
        }
    }
}
