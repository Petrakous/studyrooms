package gr.hua.dit.studyrooms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient holidayWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://date.nager.at/api/v3")
                .build();
    }
}
