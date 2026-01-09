package gr.hua.dit.studyrooms.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Spring configuration that exposes a preconfigured WebClient for the notification API.
 *
 * This class enables binding of NotificationClientProperties and creates a single
 * WebClient bean that other components can inject. Centralizing WebClient creation
 * here makes it easy to apply cross-cutting settings (base URL, headers, timeouts,
 * filters) in one place.
 */
@Configuration
@EnableConfigurationProperties(NotificationClientProperties.class)
public class NotificationConfig {

    /**
     * Build and expose a WebClient configured for the notification service.
     *
     * - Uses properties.getBaseUrl() to set the client's base URL if provided.
     * - This is the place to add default headers (e.g. API key), timeouts, or client connectors
     *   using values from NotificationClientProperties.
     *
     * @param builder   injected WebClient.Builder provided by Spring
     * @param properties bound NotificationClientProperties containing baseUrl/apiKey/timeout
     * @return a ready-to-use WebClient instance
     */
    @Bean
    public WebClient notificationWebClient(WebClient.Builder builder, NotificationClientProperties properties) {
        // Apply configured base URL only when present to avoid overwriting other builder defaults.
        String baseUrl = properties.getBaseUrl();
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            builder.baseUrl(baseUrl);
        }

        // NOTE: for real delivery:
        //  - add default headers (Authorization / API key)
        //  - configure timeouts via a client connector
        //  - add logging or retry filters
        // Keeping this method small for the demo.

        return builder.build();
    }
}
