package gr.hua.dit.studyrooms.external.notification;

public interface NotificationPort {

    void sendEmail(String recipientEmail, String subject, String body);

    void sendSms(String phoneNumber, String message);
}
