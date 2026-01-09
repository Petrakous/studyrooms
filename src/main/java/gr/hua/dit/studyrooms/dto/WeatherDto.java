
// Package declaration
package gr.hua.dit.studyrooms.dto;


// Import for date and time representation
import java.time.LocalDateTime;


/**
 * Data Transfer Object (DTO) for encapsulating weather data.
 * Contains temperature, wind speed, precipitation, weather code, and timestamp.
 */
public class WeatherDto {


    // Temperature in Celsius
    private double temperatureCelsius;

    // Wind speed
    private double windSpeed;

    // Precipitation amount
    private double precipitation;

    // Integer code representing weather condition
    private Integer weatherCode;

    // Timestamp of the weather data
    private LocalDateTime timestamp;


    /**
     * Gets the temperature in Celsius.
     */
    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    /**
     * Sets the temperature in Celsius.
     */
    public void setTemperatureCelsius(double temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }


    /**
     * Gets the wind speed.
     */
    public double getWindSpeed() {
        return windSpeed;
    }

    /**
     * Sets the wind speed.
     */
    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }


    /**
     * Gets the precipitation amount.
     */
    public double getPrecipitation() {
        return precipitation;
    }

    /**
     * Sets the precipitation amount.
     */
    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }


    /**
     * Gets the weather code.
     */
    public Integer getWeatherCode() {
        return weatherCode;
    }

    /**
     * Sets the weather code.
     */
    public void setWeatherCode(Integer weatherCode) {
        this.weatherCode = weatherCode;
    }


    /**
     * Gets the timestamp of the weather data.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the weather data.
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
