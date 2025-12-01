package gr.hua.dit.studyrooms.consumer;

import gr.hua.dit.studyrooms.consumer.dto.AuthRequest;
import gr.hua.dit.studyrooms.consumer.dto.AuthResponse;
import gr.hua.dit.studyrooms.consumer.dto.ReservationView;
import gr.hua.dit.studyrooms.consumer.dto.SpaceView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;

@Service
public class ApiConsumerClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiConsumerClient.class);

    private final WebClient webClient;
    private final ConsumerProperties consumerProperties;

    public ApiConsumerClient(WebClient.Builder builder, ConsumerProperties consumerProperties) {
        this.webClient = builder.baseUrl(consumerProperties.getBaseUrl()).build();
        this.consumerProperties = consumerProperties;
    }

    public void runDigest() {
        if (!consumerProperties.isEnabled()) {
            LOGGER.info("StudyRooms consumer is disabled (set studyrooms.consumer.enabled=true to activate)");
            return;
        }

        String token = authenticate();
        if (token == null) {
            return;
        }

        fetchSpaces(token);
        fetchMyReservations(token);
    }

    private String authenticate() {
        try {
            AuthResponse response = webClient.post()
                    .uri("/api/auth/login")
                    .bodyValue(new AuthRequest(consumerProperties.getUsername(), consumerProperties.getPassword()))
                    .retrieve()
                    .bodyToMono(AuthResponse.class)
                    .block();
            if (response == null) {
                LOGGER.warn("No token returned from StudyRooms API");
                return null;
            }
            LOGGER.info("Authenticated as {}", consumerProperties.getUsername());
            return response.token();
        } catch (Exception ex) {
            LOGGER.warn("Failed to authenticate with StudyRooms API: {}", ex.getMessage());
            return null;
        }
    }

    private void fetchSpaces(String token) {
        try {
            SpaceView[] spaces = webClient.get()
                    .uri("/api/spaces")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(SpaceView[].class)
                    .block();

            List<SpaceView> spaceList = spaces != null ? Arrays.asList(spaces) : List.of();
            LOGGER.info("Found {} study spaces", spaceList.size());
            spaceList.forEach(space -> LOGGER.info("- {} (capacity {})", space.name(), space.capacity()));
        } catch (Exception ex) {
            LOGGER.warn("Failed to fetch spaces: {}", ex.getMessage());
        }
    }

    private void fetchMyReservations(String token) {
        try {
            ReservationView[] reservations = webClient.get()
                    .uri("/api/reservations/my")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ReservationView[].class)
                    .block();

            List<ReservationView> reservationList = reservations != null ? Arrays.asList(reservations) : List.of();
            LOGGER.info("Found {} reservations for {}", reservationList.size(), consumerProperties.getUsername());
            reservationList.forEach(reservation -> LOGGER.info(
                    "- #{} on {} from {} to {} ({})",
                    reservation.id(), reservation.date(), reservation.startTime(), reservation.endTime(), reservation.status()
            ));
        } catch (Exception ex) {
            LOGGER.warn("Failed to fetch reservations: {}", ex.getMessage());
        }
    }
}
