package gr.hua.dit.studyrooms.entity;
// JPA entity representing a study space (e.g., a room) in the system

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity // Marks this class as a JPA entity
@Table(name = "study_spaces") // Maps to the 'study_spaces' table
public class StudySpace {


    @Id // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremented
    private Long id;


    @NotBlank(message = "Name is required") // Must not be blank
    @Size(max = 80, message = "Name must be at most 80 characters") // Max length 80
    @Column(nullable = false, length = 80)
    private String name; // Name of the study space


    @Size(max = 500, message = "Description must be at most 500 characters") // Max length 500
    @Column(length = 500)
    private String description; // Optional description


    @NotNull(message = "Capacity is required") // Must not be null
    @Positive(message = "Capacity must be positive") // Must be > 0
    @Column(nullable = false)
    private Integer capacity; // Number of people the space can hold


    @NotNull(message = "Open time is required")
    @Column(nullable = false)
    private LocalTime openTime; // Opening time


    @NotNull(message = "Close time is required")
    @Column(nullable = false)
    private LocalTime closeTime; // Closing time


    @OneToMany(mappedBy = "studySpace") // One study space can have many reservations
    @JsonIgnore // Ignore in JSON serialization to prevent recursion
    private List<Reservation> reservations; // Reservations for this space


    // All-args constructor
    public StudySpace(Long id, List<Reservation> reservations, LocalTime closeTime, LocalTime openTime, Integer capacity, String description, String name) {
        this.id = id;
        this.reservations = reservations;
        this.closeTime = closeTime;
        this.openTime = openTime;
        this.capacity = capacity;
        this.description = description;
        this.name = name;
    }


    // No-args constructor (required by JPA)
    public StudySpace() {
    }


    // Getters and setters
    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public List<Reservation> getReservations() {
        return reservations;
    }


    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }


    public LocalTime getCloseTime() {
        return closeTime;
    }


    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }


    public LocalTime getOpenTime() {
        return openTime;
    }


    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }


    public Integer getCapacity() {
        return capacity;
    }


    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
