package gr.hua.dit.studyrooms.external.weather;

import gr.hua.dit.studyrooms.dto.WeatherDto;
import gr.hua.dit.studyrooms.external.ExternalServiceException;
import gr.hua.dit.studyrooms.external.WeatherPort;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Service
public class OpenMeteoWeatherAdapter implements WeatherPort {

    private static final Logger logger = LoggerFactory.getLogger(OpenMeteoWeatherAdapter.class);

    private final WebClient openMeteoWebClient;

    public OpenMeteoWeatherAdapter(@Qualifier("openMeteoWebClient") WebClient openMeteoWebClient) {
        this.openMeteoWebClient = openMeteoWebClient;
    }

    @Override
    public WeatherDto getCurrentWeather(double latitude, double longitude) {
        try {
            CurrentWeatherResponse response = openMeteoWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("current", "temperature_2m,wind_speed_10m,precipitation,weather_code")
                            .queryParam("timezone", "UTC")
                            .build())
                    .retrieve()
                    .bodyToMono(CurrentWeatherResponse.class)
                    .block();

            if (response == null || response.current == null) {
                throw new ExternalServiceException("Weather service unavailable");
            }

            WeatherDto dto = new WeatherDto();
            dto.setTemperatureCelsius(response.current.temperature2m);
            dto.setWindSpeed(response.current.windSpeed10m);
            dto.setPrecipitation(response.current.precipitation);
            dto.setWeatherCode(response.current.weatherCode);
            dto.setTimestamp(LocalDateTime.parse(response.current.time));
            return dto;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Failed to fetch weather from Open-Meteo", e);
            throw new ExternalServiceException("Failed to fetch weather information", e);
        }
    }

    private static class CurrentWeatherResponse {
        private CurrentData current;

        public CurrentData getCurrent() {
            return current;
        }

        public void setCurrent(CurrentData current) {
            this.current = current;
        }
    }

    private static class CurrentData {
        private String time;
        @JsonProperty("temperature_2m")
        private double temperature2m;
        @JsonProperty("wind_speed_10m")
        private double windSpeed10m;
        private double precipitation;
        @JsonProperty("weather_code")
        private Integer weatherCode;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public double getTemperature2m() {
            return temperature2m;
        }

        public void setTemperature2m(double temperature2m) {
            this.temperature2m = temperature2m;
        }

        public double getWindSpeed10m() {
            return windSpeed10m;
        }

        public void setWindSpeed10m(double windSpeed10m) {
            this.windSpeed10m = windSpeed10m;
        }

        public double getPrecipitation() {
            return precipitation;
        }

        public void setPrecipitation(double precipitation) {
            this.precipitation = precipitation;
        }

        public Integer getWeatherCode() {
            return weatherCode;
        }

        public void setWeatherCode(Integer weatherCode) {
            this.weatherCode = weatherCode;
        }
    }
}
