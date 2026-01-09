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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Database initialization component that seeds the study room reservation system with demo data.
 * 
 * This component runs on application startup (via CommandLineRunner) only when the property
 * {@code demo.seed.enabled=true} is configured. It populates the database with:
 * - Default users (staff, student, student2)
 * - Default study spaces (Library, Reading Room, Computer Lab)
 * - Sample reservations with various statuses and dates
 * 
 * The initialization is idempotent: it checks for existing data before creating new records
 * to prevent duplicates on repeated application restarts.
 * 
 * Use case: Facilitates rapid development and testing by providing a consistent demo environment
 * without manual database setup.
 */
@Component
@ConditionalOnProperty(name = "demo.seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudySpaceRepository studySpaceRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReservationRepository reservationRepository;

    /**
     * Constructs the DataInitializer with required repository and security dependencies.
     * 
     * @param userRepository for managing user records
     * @param studySpaceRepository for managing study space records
     * @param reservationRepository for managing reservation records
     * @param passwordEncoder for securely encoding user passwords
     */
    public DataInitializer(UserRepository userRepository,
                           StudySpaceRepository studySpaceRepository,
                           ReservationRepository reservationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studySpaceRepository = studySpaceRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Executes the database initialization sequence on application startup.
     * 
     * This method:
     * 1. Creates default users (staff, student, student2) with encoded passwords
     * 2. Creates default study spaces with operating hours and capacity constraints
     * 3. Seeds sample reservations for testing the availability calculation and booking flow
     * 
     * @param args command-line arguments (unused)
     */
    @Override
    public void run(String... args) {

        // ========== SECTION 1: Initialize Default Users ==========
        // Creates three default user accounts for testing different roles and scenarios.
        // Each user is only created if it doesn't already exist (idempotent operation).
        
        // Staff user: has administrative privileges for managing spaces and reviewing reservations
        ensureUser(
                () -> userRepository.findByUsername("staff").orElse(null),
                () -> {
                    User staff = new User();
                    staff.setUsername("staff");
                    staff.setPassword(passwordEncoder.encode("staff123"));
                    staff.setFullName("Staff User");
                    staff.setEmail("staff@studyrooms.local");
                    staff.setRole(UserRole.STAFF);
                    return staff;
                }
        );

        // Primary student user: standard user for booking study spaces
        ensureUser(
                () -> userRepository.findByUsername("student").orElse(null),
                () -> {
                    User student = new User();
                    student.setUsername("student");
                    student.setPassword(passwordEncoder.encode("student123"));
                    student.setFullName("Student User");
                    student.setEmail("student@studyrooms.local");
                    student.setRole(UserRole.STUDENT);
                    return student;
                }
        );

        // Secondary student user: enables multi-user testing and realistic demo scenarios
        // (δεύτερος φοιτητής για πιο ρεαλιστικά demo)
        ensureUser(
                () -> userRepository.findByUsername("student2").orElse(null),
                () -> {
                    User student2 = new User();
                    student2.setUsername("student2");
                    student2.setPassword(passwordEncoder.encode("student123"));
                    student2.setFullName("Second Student");
                    student2.setEmail("student2@studyrooms.local");
                    student2.setRole(UserRole.STUDENT);
                    return student2;
                }
        );

        // ========== SECTION 2: Initialize Default Study Spaces ==========
        // Creates three study spaces with distinct characteristics to demonstrate
        // the reservation system's ability to handle different capacity and schedule constraints.
        
        // Large quiet study area: suitable for individual concentration, open early and late
        ensureStudySpace(
                "Library A - Quiet Zone",
                "Quiet area suitable for individual study.",
                10,
                LocalTime.of(8, 0),
                LocalTime.of(20, 0)
        );

        // Medium group study room: equipped for collaborative work, extended evening hours
        ensureStudySpace(
                "Reading Room 1",
                "Group study room with whiteboard.",
                6,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0)
        );

        // Computer lab: high capacity, limited hours for technical supervision
        ensureStudySpace(
                "Computer Lab",
                "PC lab with workstations and printer.",
                20,
                LocalTime.of(10, 0),
                LocalTime.of(18, 0)
        );

        // ========== SECTION 3: Initialize Sample Reservations ==========
        // Seeds realistic reservation data for testing availability calculations and UI flows.
        // Only runs when the database is empty to prevent duplicate data on app restarts.
        
        // Fetch the created users and spaces for reservation creation
        User student = userRepository.findByUsername("student").orElse(null);
        User student2 = userRepository.findByUsername("student2").orElse(null);
        List<StudySpace> spaces = studySpaceRepository.findAll();

        // Safety check: only populate reservations if they don't already exist.
        // This prevents duplication on repeated application startups.
        // (Για να μην γεμίζουμε διπλές, μόνο όταν η DB είναι άδεια από reservations)
        if (student != null && !spaces.isEmpty() && reservationRepository.count() == 0) {

            // Assign convenient references to the study spaces
            StudySpace roomA = spaces.get(0);
            StudySpace roomB = spaces.size() > 1 ? spaces.get(1) : roomA;
            StudySpace roomC = spaces.size() > 2 ? spaces.get(2) : roomA;

            // --- Υπάρχοντα demo (κρατάμε όπως είναι) ---
            // Fixed demo reservations: showcase different reservation states for testing

            // r1: Confirmed future reservation - typical booking
            Reservation r1 = new Reservation();
            r1.setUser(student);
            r1.setStudySpace(roomA);
            r1.setDate(LocalDate.now().plusDays(1));
            r1.setStartTime(LocalTime.of(10, 0));
            r1.setEndTime(LocalTime.of(12, 0));
            r1.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(r1);

            // r2: Pending future reservation - awaiting approval
            Reservation r2 = new Reservation();
            r2.setUser(student);
            r2.setStudySpace(roomB);
            r2.setDate(LocalDate.now().plusDays(2));
            r2.setStartTime(LocalTime.of(14, 0));
            r2.setEndTime(LocalTime.of(15, 0));
            r2.setStatus(ReservationStatus.PENDING);
            reservationRepository.save(r2);

            // r3: Cancelled by staff in the past - administrative cancellation example
            Reservation r3 = new Reservation();
            r3.setUser(student);
            r3.setStudySpace(roomA);
            r3.setDate(LocalDate.now().minusDays(1));
            r3.setStartTime(LocalTime.of(18, 0));
            r3.setEndTime(LocalTime.of(20, 0));
            r3.setStatus(ReservationStatus.CANCELLED_BY_STAFF);
            reservationRepository.save(r3);

            // r4: Cancelled by user in the past - user-initiated cancellation
            Reservation r4 = new Reservation();
            r4.setUser(student);
            r4.setStudySpace(roomB);
            r4.setDate(LocalDate.now().minusDays(2));
            r4.setStartTime(LocalTime.of(9, 0));
            r4.setEndTime(LocalTime.of(11, 0));
            r4.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(r4);

            // r5: No-show in the past - user failed to appear at reservation time
            // (NO_SHOW στο παρελθόν για να βλέπεις penalty cases κτλ)
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

            // r6: Confirmed reservation for today - active ongoing booking
            // (Μια κρατηση σήμερα, confirmed)
            Reservation r6 = new Reservation();
            r6.setUser(student);
            r6.setStudySpace(roomC);
            r6.setDate(LocalDate.now());
            r6.setStartTime(LocalTime.of(16, 0));
            r6.setEndTime(LocalTime.of(18, 0));
            r6.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(r6);

            // --- Random demo data: generates varied test data on each fresh database ---
            // (διαφορετικά κάθε run σε φρέσκια DB)
            // Creates realistic variety to test the system's robustness and availability calculations

            Random random = new Random();

            // Generate 10 random reservations with varied parameters
            // (10 random reservations για student / student2 σε διάφορους χώρους & μέρες)
            for (int i = 0; i < 10; i++) {

                // Alternate between student and student2 (or use student if student2 doesn't exist)
                User randomUser = (i % 2 == 0 || student2 == null) ? student : student2;
                
                // Select a random study space
                StudySpace randomSpace = spaces.get(random.nextInt(spaces.size()));

                // Generate a date within a realistic range: 3 days ago to 7 days in future
                // (random μέρα από 3 μέρες πριν μέχρι 7 μέρες μετά)
                int dayOffset = random.nextInt(11) - 3; // Range: -3 to +7
                LocalDate date = LocalDate.now().plusDays(dayOffset);

                // Generate random start and end times within the space's operating hours
                // (χρονικό διάστημα 1 ή 2 ωρών μέσα στο ωράριο του χώρου)
                LocalTime open = randomSpace.getOpenTime();
                LocalTime close = randomSpace.getCloseTime();

                // Calculate available hours, ensuring safe slot selection
                // (να έχουμε χώρο τουλάχιστον 2 ώρες για πιο safe επιλογή)
                int availableHours = Math.max(1, close.getHour() - open.getHour() - 1);
                int startOffsetHours = availableHours > 0 ? random.nextInt(availableHours) : 0;

                LocalTime start = open.plusHours(startOffsetHours);
                LocalTime end = start.plusHours(random.nextBoolean() ? 1 : 2);
                
                // Ensure end time doesn't exceed closing time
                if (end.isAfter(close)) {
                    end = close;
                }

                // Randomly assign a reservation status to test various states
                // (random status)
                ReservationStatus status;
                int roll = random.nextInt(5);
                switch (roll) {
                    case 0 -> status = ReservationStatus.CONFIRMED;
                    case 1 -> status = ReservationStatus.PENDING;
                    case 2 -> status = ReservationStatus.CANCELLED;
                    case 3 -> status = ReservationStatus.CANCELLED_BY_STAFF;
                    default -> status = ReservationStatus.NO_SHOW;
                }

                // Create and save the random reservation
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

    /**
     * Ensures a user exists in the database, creating it if not found.
     * 
     * This helper method implements an idempotent pattern: it checks whether a user already
     * exists using a supplied query function, and only creates and saves a new user if the
     * query returns null. This prevents duplicate user records on repeated application runs.
     * 
     * @param existingSupplier a lambda that queries the repository for an existing user
     *                         (typically: {@code () -> userRepository.findByUsername("name").orElse(null)})
     * @param creator a lambda that constructs and returns a new User object with all required fields set
     * @return the existing user if found, or the newly created and saved user
     */
    private User ensureUser(Supplier<User> existingSupplier, Supplier<User> creator) {
        User existing = existingSupplier.get();
        if (existing != null) {
            return existing;
        }
        User newUser = creator.get();
        return newUser != null ? userRepository.save(newUser) : null;
    }

    /**
     * Ensures a study space exists in the database with the given configuration.
     * 
     * This helper method provides idempotent creation: if a space with the given name exists,
     * it updates its properties (description, capacity, hours) with the provided values.
     * Otherwise, it creates a new space record. This approach ensures the database always has
     * the latest space configuration without duplicates.
     * 
     * @param name the unique name of the study space
     * @param description a text description of the space's purpose and amenities
     * @param capacity the maximum number of simultaneous reservations allowed
     * @param openTime the space's opening hour (e.g., 8:00 AM)
     * @param closeTime the space's closing hour (e.g., 8:00 PM)
     * @return the existing space (possibly updated) or the newly created space
     */
    private StudySpace ensureStudySpace(String name, String description, int capacity, LocalTime openTime, LocalTime closeTime) {
        return studySpaceRepository.findByName(name)
                .map(existing -> {
                    existing.setDescription(description);
                    existing.setCapacity(capacity);
                    existing.setOpenTime(openTime);
                    existing.setCloseTime(closeTime);
                    return studySpaceRepository.save(existing);
                })
                .orElseGet(() -> {
                    StudySpace space = new StudySpace();
                    space.setName(name);
                    space.setDescription(description);
                    space.setCapacity(capacity);
                    space.setOpenTime(openTime);
                    space.setCloseTime(closeTime);
                    return studySpaceRepository.save(space);
                });
    }
}
