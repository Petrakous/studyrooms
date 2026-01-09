
// Package for external service interfaces
package gr.hua.dit.studyrooms.external;


import gr.hua.dit.studyrooms.dto.WeatherDto;
import java.time.LocalDateTime;


/**
 * Interface for accessing weather data from an external source.
 * Implementations may fetch data from APIs or other services.
 */
public interface WeatherPort {


    /**
     * Gets the current weather for the specified geographic coordinates.
     *
     * @param latitude  the latitude of the location
     * @param longitude the longitude of the location
     * @return WeatherDto containing current weather information
     */
    WeatherDto getCurrentWeather(double latitude, double longitude);


    /**
     * Gets the weather for the specified geographic coordinates at a specific date and time.
     *
     * @param latitude  the latitude of the location
     * @param longitude the longitude of the location
     * @param at        the date and time for which to retrieve the weather
     * @return WeatherDto containing weather information for the given time
     */
    WeatherDto getWeatherAt(double latitude, double longitude, LocalDateTime at);
}
