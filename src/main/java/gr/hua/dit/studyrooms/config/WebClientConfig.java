package gr.hua.dit.studyrooms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Spring configuration that creates preconfigured WebClient beans for external third-party APIs.
 * 
 * This class centralizes the configuration of HTTP clients for calling external services
 * (holiday data, weather data, etc). Each bean represents a distinct external API with its
 * own base URL and potentially its own timeout/header/error handling strategies.
 * 
 * Services can inject these beans via @Autowired or @Qualifier to make calls to the
 * respective APIs without having to construct or configure WebClient instances themselves.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates a WebClient configured for the Nager.at public holiday API.
     * 
     * The Nager.at API provides holiday information for various countries and dates.
     * This client is used to check whether a given date is a holiday, which may affect
     * availability calculations or reservation policies for study spaces.
     * 
     * Base URL: https://date.nager.at/api/v3
     * 
     * @param builder injected WebClient.Builder provided by Spring
     * @return a WebClient preconfigured with the holiday API base URL
     */
    @Bean
    public WebClient holidayWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://date.nager.at/api/v3")
                .build();
    }

    /**
     * Creates a WebClient configured for the Open-Meteo weather API.
     * 
     * The Open-Meteo API provides weather forecasts and historical weather data.
     * This client is used to retrieve weather conditions for display alongside study
     * space availability or for analytics/reporting purposes.
     * 
     * Base URL: https://api.open-meteo.com/v1
     * 
     * @param builder injected WebClient.Builder provided by Spring
     * @return a WebClient preconfigured with the weather API base URL
     */
    @Bean
    public WebClient openMeteoWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.open-meteo.com/v1")
                .build();
    }
}
