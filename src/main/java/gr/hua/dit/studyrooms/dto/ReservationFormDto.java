package gr.hua.dit.studyrooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Reservation request payload used by both HTML forms and REST API")
public class ReservationFormDto {

    @Schema(description = "ID of the study space to reserve", example = "1")
    @NotNull(message = "Study space is required")
    private Long studySpaceId;

    @Schema(description = "Reservation date", example = "2024-09-15")
    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Reservation date cannot be in the past")
    private LocalDate date;

    @Schema(description = "Start time of the reservation", example = "10:00")
    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @Schema(description = "End time of the reservation", example = "12:00")
    @NotNull(message = "End time is required")
    private LocalTime endTime;

    public ReservationFormDto() {
    }

    public Long getStudySpaceId() {
        return studySpaceId;
    }

    public void setStudySpaceId(Long studySpaceId) {
        this.studySpaceId = studySpaceId;
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
}
