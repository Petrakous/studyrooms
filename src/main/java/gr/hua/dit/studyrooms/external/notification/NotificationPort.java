
// Package declaration for the notification port interface
package gr.hua.dit.studyrooms.external.notification;


/**
 * Interface defining notification methods for sending emails and SMS messages.
 * Implementations of this interface provide the actual notification logic.
 */
public interface NotificationPort {


    /**
     * Sends an email notification to the specified recipient.
     *
     * @param recipientEmail the email address of the recipient
     * @param subject the subject of the email
     * @param body the body content of the email
     */
    void sendEmail(String recipientEmail, String subject, String body);


    /**
     * Sends an SMS notification to the specified phone number.
     *
     * @param phoneNumber the recipient's phone number
     * @param message the SMS message content
     */
    void sendSms(String phoneNumber, String message);
}
