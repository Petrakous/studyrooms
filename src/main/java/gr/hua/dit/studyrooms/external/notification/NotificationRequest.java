package gr.hua.dit.studyrooms.external.notification;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for the external notification provider")
public class NotificationRequest {
    @Schema(description = "Delivery channel (email or sms)")
    private String channel;

    @Schema(description = "Recipient email or phone")
    private String recipient;

    @Schema(description = "Subject for email notifications")
    private String subject;

    @Schema(description = "Body of the notification")
    private String body;

    public NotificationRequest() {
    }

    public NotificationRequest(String channel, String recipient, String subject, String body) {
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
