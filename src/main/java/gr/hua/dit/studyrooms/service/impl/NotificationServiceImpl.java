package gr.hua.dit.studyrooms.service.impl;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.external.notification.NotificationPort;
import gr.hua.dit.studyrooms.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificationPort notificationPort;

    public NotificationServiceImpl(NotificationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

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

        String subject = "StudyRooms reservation confirmed";
        String body = String.format(
                "Hello %s,%nYour reservation for %s on %s from %s to %s is confirmed.",
                reservation.getUser().getFullName(),
                reservation.getStudySpace().getName(),
                DATE_FORMAT.format(reservation.getDate()),
                TIME_FORMAT.format(reservation.getStartTime()),
                TIME_FORMAT.format(reservation.getEndTime())
        );

        safely(() -> notificationPort.sendEmail(recipientEmail, subject, body));
    }

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

        safely(() -> notificationPort.sendEmail(recipientEmail, subject, body));
    }

    private void safely(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            LOGGER.warn("Notification delivery failed: {}", ex.getMessage());
        }
    }
}
