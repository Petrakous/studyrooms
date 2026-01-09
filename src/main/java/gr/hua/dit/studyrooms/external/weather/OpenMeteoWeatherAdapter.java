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
import java.time.format.DateTimeFormatter;
import java.util.List;


// Adapter for fetching weather data from the Open-Meteo API
@Service
public class OpenMeteoWeatherAdapter implements WeatherPort {


    // Logger for logging warnings and errors
    private static final Logger logger = LoggerFactory.getLogger(OpenMeteoWeatherAdapter.class);


    // WebClient instance for making HTTP requests to Open-Meteo
    private final WebClient openMeteoWebClient;


    // Constructor injection of the WebClient bean
    public OpenMeteoWeatherAdapter(@Qualifier("openMeteoWebClient") WebClient openMeteoWebClient) {
        this.openMeteoWebClient = openMeteoWebClient;
    }

    @Override

    /**
     * Fetches the current weather for the given latitude and longitude from Open-Meteo API.
     * Maps the response to WeatherDto.
     */
    public WeatherDto getCurrentWeather(double latitude, double longitude) {
        try {
            // Build and execute the GET request to Open-Meteo
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

            // Check for null response
            if (response == null || response.current == null) {
                throw new ExternalServiceException("Weather service unavailable");
            }

            // Map API response to WeatherDto
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

    @Override

    /**
     * Fetches the weather forecast for a specific date and time.
     * If 'at' is null, returns the current weather.
     * Otherwise, finds the closest available forecast data for the requested time.
     */
    public WeatherDto getWeatherAt(double latitude, double longitude, LocalDateTime at) {
        if (at == null) {
            return getCurrentWeather(latitude, longitude);
        }

        try {
            // Format date and normalize time to the hour
            String date = at.toLocalDate().toString();
            LocalDateTime normalized = at.withMinute(0).withSecond(0).withNano(0);
            String target = normalized.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // Build and execute the GET request for hourly forecast
            ForecastResponse response = openMeteoWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("hourly", "temperature_2m,wind_speed_10m,precipitation,weather_code")
                            .queryParam("start_date", date)
                            .queryParam("end_date", date)
                            .queryParam("timezone", "UTC")
                            .build())
                    .retrieve()
                    .bodyToMono(ForecastResponse.class)
                    .block();

            // Check for null response
            if (response == null || response.hourly == null || response.hourly.time == null) {
                throw new ExternalServiceException("Weather service unavailable");
            }

            // Try to find the exact or closest time index in the forecast
            int index = findExactIndex(response.hourly.time, target);
            if (index == -1) {
                index = findClosestIndex(response.hourly.time, normalized);
            }

            // If no valid index found, throw exception
            if (index == -1 || !response.hourly.hasValuesAt(index)) {
                throw new ExternalServiceException("No forecast data available for requested time");
            }

            // Map forecast data to WeatherDto
            WeatherDto dto = new WeatherDto();
            dto.setTimestamp(LocalDateTime.parse(response.hourly.time.get(index)));
            dto.setTemperatureCelsius(response.hourly.temperature2m.get(index));
            dto.setWindSpeed(response.hourly.windSpeed10m.get(index));
            dto.setPrecipitation(response.hourly.precipitation.get(index));
            dto.setWeatherCode(response.hourly.weatherCode.get(index));
            return dto;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Failed to fetch forecast from Open-Meteo", e);
            throw new ExternalServiceException("Failed to fetch weather information", e);
        }
    }


    /**
     * Finds the index of the exact time string in the list.
     * Returns -1 if not found.
     */
    private int findExactIndex(List<String> times, String target) {
        for (int i = 0; i < times.size(); i++) {
            if (target.equals(times.get(i))) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Finds the index of the time closest to the target LocalDateTime.
     * Returns -1 if the list is empty.
     */
    private int findClosestIndex(List<String> times, LocalDateTime target) {
        int closestIndex = -1;
        long smallestDiff = Long.MAX_VALUE;
        for (int i = 0; i < times.size(); i++) {
            LocalDateTime candidate = LocalDateTime.parse(times.get(i));
            long diff = Math.abs(candidate.atZone(java.time.ZoneOffset.UTC).toEpochSecond()
                    - target.atZone(java.time.ZoneOffset.UTC).toEpochSecond());
            if (diff < smallestDiff) {
                smallestDiff = diff;
                closestIndex = i;
            }
        }
        return closestIndex;
    }


    // Internal class for mapping the current weather API response
    private static class CurrentWeatherResponse {
        private CurrentData current;

        public CurrentData getCurrent() {
            return current;
        }

        public void setCurrent(CurrentData current) {
            this.current = current;
        }
    }


    // Internal class for mapping the 'current' field in the API response
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


    // Internal class for mapping the forecast API response
    private static class ForecastResponse {
        private Hourly hourly;

        public Hourly getHourly() {
            return hourly;
        }

        public void setHourly(Hourly hourly) {
            this.hourly = hourly;
        }
    }


    // Internal class for mapping the 'hourly' field in the forecast response
    private static class Hourly {
        private List<String> time;
        @JsonProperty("temperature_2m")
        private List<Double> temperature2m;
        @JsonProperty("wind_speed_10m")
        private List<Double> windSpeed10m;
        private List<Double> precipitation;
        @JsonProperty("weather_code")
        private List<Integer> weatherCode;

        public List<String> getTime() {
            return time;
        }

        public void setTime(List<String> time) {
            this.time = time;
        }

        public void setTemperature2m(List<Double> temperature2m) {
            this.temperature2m = temperature2m;
        }

        public void setWindSpeed10m(List<Double> windSpeed10m) {
            this.windSpeed10m = windSpeed10m;
        }

        public void setPrecipitation(List<Double> precipitation) {
            this.precipitation = precipitation;
        }

        public void setWeatherCode(List<Integer> weatherCode) {
            this.weatherCode = weatherCode;
        }

        /**
         * Checks if all weather value lists have a value at the given index.
         */
        boolean hasValuesAt(int index) {
            return temperature2m != null && windSpeed10m != null && precipitation != null && weatherCode != null
                    && index < temperature2m.size() && index < windSpeed10m.size()
                    && index < precipitation.size() && index < weatherCode.size();
        }
    }
}
