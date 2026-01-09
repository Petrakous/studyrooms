
// Package declaration for the notification external module
package gr.hua.dit.studyrooms.external.notification;


// Import Swagger annotation for API schema documentation
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * NotificationRequest represents the payload for sending notifications (email or SMS)
 * via an external provider. It contains all necessary information for the notification.
 */
@Schema(description = "Payload for the external notification provider")
public class NotificationRequest {
    /**
     * Delivery channel for the notification (e.g., "email" or "sms").
     */
    @Schema(description = "Delivery channel (email or sms)")
    private String channel;

    /**
     * Recipient's email address or phone number, depending on the channel.
     */
    @Schema(description = "Recipient email or phone")
    private String recipient;

    /**
     * Subject of the notification (used for emails).
     */
    @Schema(description = "Subject for email notifications")
    private String subject;

    /**
     * Body content of the notification message.
     */
    @Schema(description = "Body of the notification")
    private String body;

    /**
     * Default constructor required for serialization/deserialization.
     */
    public NotificationRequest() {
    }

    /**
     * Constructs a NotificationRequest with all fields initialized.
     *
     * @param channel   Delivery channel (email or sms)
     * @param recipient Recipient email or phone
     * @param subject   Subject for email notifications
     * @param body      Body of the notification
     */
    public NotificationRequest(String channel, String recipient, String subject, String body) {
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    /**
     * Gets the delivery channel.
     * @return channel (email or sms)
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Sets the delivery channel.
     * @param channel (email or sms)
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Gets the recipient's email or phone.
     * @return recipient
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Sets the recipient's email or phone.
     * @param recipient email or phone
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     * Gets the subject (for email notifications).
     * @return subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject (for email notifications).
     * @param subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the body of the notification.
     * @return body
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the body of the notification.
     * @param body
     */
    public void setBody(String body) {
        this.body = body;
    }
}
