package gr.hua.dit.studyrooms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for the notification API client.
 * <p>
 * Properties are bound from configuration keys with prefix {@code notification.api}.
 * Example keys: {@code notification.api.baseUrl}, {@code notification.api.apiKey},
 * {@code notification.api.enabled}, {@code notification.api.timeout}.
 */
@ConfigurationProperties(prefix = "notification.api")
public class NotificationClientProperties {

    /**
     * The base URL of the external notification service. In this exercise the value
     * is a placeholder; in production you would set this to the real endpoint.
     */
    private String baseUrl = "https://example.com/notifications";

    /**
     * API key or token used to authenticate with the notification service.
     * Keep this secret in production (environment variable or secret manager).
     */
    private String apiKey = "demo-key";

    /**
     * Feature flag to enable or disable actual notification delivery. When {@code false}
     * the application can perform a dry-run (for example log the message) instead of
     * sending requests to the external service.
     */
    private boolean enabled = false;

    /**
     * HTTP client timeout for notification API calls. Uses {@link java.time.Duration}
     * for readable configuration (e.g. "5s" in YAML/properties).
     */
    private Duration timeout = Duration.ofSeconds(3);

    /**
     * Returns the configured base URL for the notification API.
     *
     * @return base URL string
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base URL for the notification API.
     *
     * @param baseUrl the API base URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the API key used for authentication with the notification service.
     *
     * @return API key string
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API key for the notification service. In production prefer using
     * environment variables or a secrets manager rather than committing keys.
     *
     * @param apiKey API key string
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Indicates whether the notification client should perform real deliveries.
     * When {@code false} the application can log or simulate notifications instead
     * of issuing network calls.
     *
     * @return true if notifications are enabled, false for dry-run
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable sending notifications.
     *
     * @param enabled true to enable deliveries, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the configured timeout used for notification API calls.
     *
     * @return Duration representing the timeout
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Set the HTTP client timeout for notification requests.
     *
     * @param timeout Duration to wait for responses before timing out
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
