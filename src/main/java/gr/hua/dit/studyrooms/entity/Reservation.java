
// Reservation entity represents a booking of a study space by a user for a specific date and time range.
// It is mapped to the 'reservations' table in the database.
package gr.hua.dit.studyrooms.entity;


import jakarta.persistence.*; // JPA annotations for ORM mapping
import java.time.LocalDate;  // For reservation date
import java.time.LocalTime;  // For reservation start/end times


@Entity // Marks this class as a JPA entity
@Table(name = "reservations") // Maps to 'reservations' table
public class Reservation {


    @Id // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremented by DB
    private Long id;


    // The user who made the reservation (many reservations can belong to one user)
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id") // Foreign key to User
    private User user;


    // The study space being reserved (many reservations can be for one study space)
    @ManyToOne(optional = false)
    @JoinColumn(name = "study_space_id") // Foreign key to StudySpace
    private StudySpace studySpace;


    @Column(nullable = false) // Reservation date (required)
    private LocalDate date;


    @Column(nullable = false) // Start time of reservation (required)
    private LocalTime startTime;


    @Column(nullable = false) // End time of reservation (required)
    private LocalTime endTime;


    @Enumerated(EnumType.STRING) // Store enum as string in DB
    @Column(nullable = false, length = 20) // Reservation status (required)
    private ReservationStatus status;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean demo;
    
    // Full constructor for Reservation
    public Reservation(Long id, User user, StudySpace studySpace, LocalDate date, LocalTime startTime,
                       LocalTime endTime, ReservationStatus status, boolean demo) {
        this.id = id;
        this.user = user;
        this.studySpace = studySpace;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.demo = demo;
    }


    // Default constructor required by JPA
    public Reservation() {
    }


    // Getters and setters for all fields
    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public User getUser() {
        return user;
    }


    public void setUser(User user) {
        this.user = user;
    }


    public StudySpace getStudySpace() {
        return studySpace;
    }


    public void setStudySpace(StudySpace studySpace) {
        this.studySpace = studySpace;
    }


    public LocalDate getDate() {
        return date;
    }


    public void setDate(LocalDate date) {
        this.date = date;
    }


    public LocalTime getStartTime() {
        return startTime;
    }


    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }


    public LocalTime getEndTime() {
        return endTime;
    }


    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }


    public ReservationStatus getStatus() {
        return status;
    }


    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public boolean isDemo() {
        return demo;
    }

    public void setDemo(boolean demo) {
        this.demo = demo;
    }
}
