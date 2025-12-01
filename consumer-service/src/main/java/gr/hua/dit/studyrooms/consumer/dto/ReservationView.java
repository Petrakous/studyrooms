package gr.hua.dit.studyrooms.consumer.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationView(Long id, LocalDate date, LocalTime startTime, LocalTime endTime, String status) { }
