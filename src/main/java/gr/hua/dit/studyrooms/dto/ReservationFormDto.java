package gr.hua.dit.studyrooms.dto;
// Data Transfer Object (DTO) for handling reservation form submissions
// Used for both HTML forms and REST API requests
// Importing necessary annotations for validation and API documentation

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

// Represents a reservation request payload
@Schema(description = "Reservation request payload used by both HTML forms and REST API")
public class ReservationFormDto {


    // The ID of the study space to reserve
    @Schema(description = "ID of the study space to reserve", example = "1")
    @NotNull(message = "Study space is required")
    private Long studySpaceId;


    // The date for the reservation (must be today or in the future)
    @Schema(description = "Reservation date", example = "2024-09-15")
    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Reservation date cannot be in the past")
    private LocalDate date;


    // The start time of the reservation
    @Schema(description = "Start time of the reservation", example = "10:00")
    @NotNull(message = "Start time is required")
    private LocalTime startTime;


    // The end time of the reservation
    @Schema(description = "End time of the reservation", example = "12:00")
    @NotNull(message = "End time is required")
    private LocalTime endTime;


    // Default constructor
    public ReservationFormDto() {
    }


    // Getter and setter for studySpaceId
    public Long getStudySpaceId() {
        return studySpaceId;
    }


    public void setStudySpaceId(Long studySpaceId) {
        this.studySpaceId = studySpaceId;
    }


    // Getter and setter for date
    public LocalDate getDate() {
        return date;
    }


    public void setDate(LocalDate date) {
        this.date = date;
    }


    // Getter and setter for startTime
    public LocalTime getStartTime() {
        return startTime;
    }


    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }


    // Getter and setter for endTime
    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
