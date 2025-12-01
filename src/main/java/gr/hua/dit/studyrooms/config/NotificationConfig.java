package gr.hua.dit.studyrooms.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(NotificationClientProperties.class)
public class NotificationConfig {

    @Bean
    public WebClient notificationWebClient(WebClient.Builder builder, NotificationClientProperties properties) {
        return builder
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
