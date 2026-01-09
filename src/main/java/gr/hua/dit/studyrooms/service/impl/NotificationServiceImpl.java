package gr.hua.dit.studyrooms.service.impl;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.external.notification.NotificationPort;
import gr.hua.dit.studyrooms.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;


/**
 * Service implementation for sending reservation-related notifications via email.
 * Uses NotificationPort to abstract the actual email sending logic.
 */
@Service
public class NotificationServiceImpl implements NotificationService {


    // Logger for logging debug and warning messages
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);
    // Formatter for reservation date
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // Formatter for reservation time
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");


    // Port for sending notifications (e.g., email)
    private final NotificationPort notificationPort;

    /**
     * Constructor for dependency injection of NotificationPort.
     */
    public NotificationServiceImpl(NotificationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    /**
     * Sends an email notification to the user when a reservation is created.
     * Checks for null reservation or user, and ensures email is present.
     * @param reservation the created reservation
     */
    @Override
    public void notifyReservationCreated(Reservation reservation) {
        if (reservation == null || reservation.getUser() == null) {
            return;
        }

        String recipientEmail = reservation.getUser().getEmail();
        if (recipientEmail == null || recipientEmail.isBlank()) {
            LOGGER.debug("No email available for reservation {}", reservation.getId());
            return;
        }

        // Prepare email subject and body with reservation details
        String subject = "StudyRooms reservation confirmed";
        String body = String.format(
                "Hello %s,%nYour reservation for %s on %s from %s to %s is confirmed.",
                reservation.getUser().getFullName(),
                reservation.getStudySpace().getName(),
                DATE_FORMAT.format(reservation.getDate()),
                TIME_FORMAT.format(reservation.getStartTime()),
                TIME_FORMAT.format(reservation.getEndTime())
        );

        // Send the email, handling any exceptions
        safely(() -> notificationPort.sendEmail(recipientEmail, subject, body));
    }

    /**
     * Sends an email notification to the user when a reservation is cancelled.
     * The subject and message differ if cancelled by staff.
     * @param reservation the cancelled reservation
     * @param cancelledByStaff true if cancelled by staff, false if by user
     */
    @Override
    public void notifyReservationCancelled(Reservation reservation, boolean cancelledByStaff) {
        if (reservation == null || reservation.getUser() == null) {
            return;
        }

        String recipientEmail = reservation.getUser().getEmail();
        if (recipientEmail == null || recipientEmail.isBlank()) {
            LOGGER.debug("No email available for reservation {}", reservation.getId());
            return;
        }

        // Choose subject and body based on who cancelled
        String subject = cancelledByStaff
                ? "StudyRooms reservation cancelled by staff"
                : "Your StudyRooms reservation was cancelled";
        String body = String.format(
                "Hello %s,%nYour reservation for %s on %s was cancelled%s.",
                reservation.getUser().getFullName(),
                reservation.getStudySpace().getName(),
                DATE_FORMAT.format(reservation.getDate()),
                cancelledByStaff ? " by staff" : ""
        );

        // Send the email, handling any exceptions
        safely(() -> notificationPort.sendEmail(recipientEmail, subject, body));
    }

    /**
     * Runs the given Runnable and logs a warning if an exception occurs.
     * Used to prevent notification failures from affecting main flow.
     * @param runnable the code to run safely
     */
    private void safely(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            LOGGER.warn("Notification delivery failed: {}", ex.getMessage());
        }
    }
}
