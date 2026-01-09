package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.dto.WeatherDto;
import java.time.LocalDateTime;

/**
 * Service interface for retrieving weather information.
 * Provides methods to fetch weather data based on geographic coordinates,
 * which can be used to display weather conditions for study room locations.
 */
public interface WeatherService {

    /**
     * Retrieves the current weather conditions at the specified location.
     *
     * @param latitude  the latitude coordinate of the location
     * @param longitude the longitude coordinate of the location
     * @return a {@link WeatherDto} containing current weather data
     */
    WeatherDto getCurrentWeather(double latitude, double longitude);

    /**
     * Retrieves weather data for a specific point in time at the specified location.
     * Can be used to fetch historical weather data or weather forecasts.
     *
     * @param latitude  the latitude coordinate of the location
     * @param longitude the longitude coordinate of the location
     * @param at        the date and time for which to retrieve weather data
     * @return a {@link WeatherDto} containing weather data for the specified time
     */
    WeatherDto getWeather(double latitude, double longitude, LocalDateTime at);
}
