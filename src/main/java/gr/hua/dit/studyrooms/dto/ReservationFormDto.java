package gr.hua.dit.studyrooms.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationFormDto {

    @NotNull(message = "Study space is required")
    private Long studySpaceId;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Reservation date cannot be in the past")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

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
