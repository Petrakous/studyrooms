package gr.hua.dit.studyrooms.external.notification;

import gr.hua.dit.studyrooms.config.NotificationClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class NotificationApiAdapter implements NotificationPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationApiAdapter.class);

    private final WebClient notificationClient;
    private final NotificationClientProperties properties;

    public NotificationApiAdapter(WebClient notificationWebClient,
                                  NotificationClientProperties properties) {
        this.notificationClient = notificationWebClient;
        this.properties = properties;
    }

    @Override
    public void sendEmail(String recipientEmail, String subject, String body) {
        send("email", recipientEmail, subject, body);
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        send("sms", phoneNumber, null, message);
    }

    private void send(String channel, String recipient, String subject, String body) {
        if (!properties.isEnabled()) {
            String messageDetails = subject != null ?
                    String.format("subject '%s' and body '%s'", subject, body) :
                    String.format("message '%s'", body);

            LOGGER.info("Notification client disabled; skipping {} to {} with {}", channel, recipient, messageDetails);
            return;
        }

        NotificationRequest payload = new NotificationRequest(channel, recipient, subject, body);

        try {
            notificationClient
                    .post()
                    .uri("/notify")
                    .header("X-API-KEY", properties.getApiKey())
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block(properties.getTimeout());
        } catch (WebClientResponseException ex) {
            LOGGER.warn("Notification service returned {} for {} to {}: {}", ex.getStatusCode(), channel, recipient, ex.getResponseBodyAsString());
        } catch (Exception ex) {
            LOGGER.warn("Notification service call failed for {} to {}: {}", channel, recipient, ex.getMessage());
        }
    }
}
