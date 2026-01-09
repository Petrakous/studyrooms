package gr.hua.dit.studyrooms.external.notification;

import gr.hua.dit.studyrooms.config.NotificationClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


// Spring component that adapts notification sending to an external API
@Component
public class NotificationApiAdapter implements NotificationPort {


    // Logger for logging info and warnings
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationApiAdapter.class);


    // WebClient for making HTTP requests to the notification service
    private final WebClient notificationClient;
    // Configuration properties for the notification client (API key, timeout, enabled flag)
    private final NotificationClientProperties properties;


    // Constructor injects the WebClient and configuration properties
    public NotificationApiAdapter(WebClient notificationWebClient,
                                  NotificationClientProperties properties) {
        this.notificationClient = notificationWebClient;
        this.properties = properties;
    }


    // Sends an email notification
    @Override
    public void sendEmail(String recipientEmail, String subject, String body) {
        send("email", recipientEmail, subject, body);
    }


    // Sends an SMS notification (Not Implemented)
    @Override
    public void sendSms(String phoneNumber, String message) {
        send("sms", phoneNumber, null, message);
    }


    /**
     * Sends a notification (email or SMS) to the external notification service.
     * If notifications are disabled, logs and skips sending.
     * Handles and logs errors from the notification service.
     *
     * @param channel   The notification channel ("email" or "sms")
     * @param recipient The recipient's email or phone number
     * @param subject   The subject (for email), null for SMS
     * @param body      The message body
     */
    private void send(String channel, String recipient, String subject, String body) {
        // If notifications are disabled, log and skip sending
        if (!properties.isEnabled()) {
            String messageDetails = subject != null ?
                    String.format("subject '%s' and body '%s'", subject, body) :
                    String.format("message '%s'", body);

            LOGGER.info("Notification client disabled; skipping {} to {} with {}", channel, recipient, messageDetails);
            return;
        }

        // Build the notification request payload
        NotificationRequest payload = new NotificationRequest(channel, recipient, subject, body);

        try {
            // Send POST request to /notify endpoint with API key and payload
            notificationClient
                    .post()
                    .uri("/notify")
                    .header("X-API-KEY", properties.getApiKey())
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block(properties.getTimeout());
        } catch (WebClientResponseException ex) {
            // Log warning if the notification service returns an error response
            LOGGER.warn("Notification service returned {} for {} to {}: {}", ex.getStatusCode(), channel, recipient, ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Log warning for any other exception
            LOGGER.warn("Notification service call failed for {} to {}: {}", channel, recipient, ex.getMessage());
        }
    }
}
