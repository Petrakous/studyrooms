
/**
 * User entity class representing a user in the system.
 * Maps to the 'users' table in the database.
 * Contains user credentials, role, reservations, and penalty information.
 */
package gr.hua.dit.studyrooms.entity;


import jakarta.persistence.*; // JPA annotations for ORM
import java.util.List;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore; // For controlling JSON serialization


@Entity // JPA annotation to mark this class as a persistent entity
@Table(name = "users") // Specifies the table name in the database
public class User {


    @Id // Primary key for the entity
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generated value (auto-increment)
    private Long id;


    @Column(nullable = false, unique = true, length = 50) // Username must be unique and not null
    private String username;


    @Column(nullable = false) // Password must not be null
    @JsonIgnore // Exclude password from JSON serialization for security
    private String password;


    @Column(nullable = false, length = 100) // Full name of the user (required)
    private String fullName;


    @Column(nullable = false, unique = true, length = 100) // Email must be unique and not null
    @JsonIgnore // Exclude email from JSON serialization for privacy
    private String email;


    @Enumerated(EnumType.STRING) // Store enum as string in the database
    @Column(nullable = false, length = 20) // Role is required
    private UserRole role; // User's role (e.g., ADMIN, USER)


    @OneToMany(mappedBy = "user") // One user can have many reservations (inverse side)
    @JsonIgnore // Prevent circular reference and unnecessary data in JSON
    private List<Reservation> reservations;


    @Column(name = "penalty_until") // Date until which the user is penalized (if any)
    private LocalDate penaltyUntil;


    /**
     * Full-argument constructor for User.
     * @param id User ID
     * @param reservations List of reservations
     * @param role User role
     * @param email User email
     * @param fullName User's full name
     * @param password User password
     * @param username User username
     */
    public User(Long id, List<Reservation> reservations, UserRole role,
                String email, String fullName, String password, String username) {
        this.id = id;
        this.reservations = reservations;
        this.role = role;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.username = username;
    }

    /**
     * Default constructor required by JPA.
     */
    public User() {}



    /**
     * Gets the user ID.
     * @return User ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user ID.
     * @param id User ID
     */
    public void setId(Long id) {
        this.id = id;
    }



    /**
     * Gets the username.
     * @return Username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * @param username Username
     */
    public void setUsername(String username) {
        this.username = username;
    }



    /**
     * Gets the password.
     * @return Password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     * @param password Password
     */
    public void setPassword(String password) {
        this.password = password;
    }



    /**
     * Gets the full name of the user.
     * @return Full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name of the user.
     * @param fullName Full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }



    /**
     * Gets the email address.
     * @return Email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     * @param email Email
     */
    public void setEmail(String email) {
        this.email = email;
    }



    /**
     * Gets the user role.
     * @return UserRole
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Sets the user role.
     * @param role UserRole
     */
    public void setRole(UserRole role) {
        this.role = role;
    }



    /**
     * Gets the list of reservations for the user.
     * @return List of reservations
     */
    public List<Reservation> getReservations() {
        return reservations;
    }

    /**
     * Sets the list of reservations for the user.
     * @param reservations List of reservations
     */
    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }



    /**
     * Gets the penalty expiration date.
     * @return Penalty expiration date
     */
    public LocalDate getPenaltyUntil() {
        return penaltyUntil;
    }

    /**
     * Sets the penalty expiration date.
     * @param penaltyUntil Penalty expiration date
     */
    public void setPenaltyUntil(LocalDate penaltyUntil) {
        this.penaltyUntil = penaltyUntil;
    }
}
