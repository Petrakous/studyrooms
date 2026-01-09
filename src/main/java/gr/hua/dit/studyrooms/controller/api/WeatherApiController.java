package gr.hua.dit.studyrooms.controller.api;

import gr.hua.dit.studyrooms.dto.WeatherDto;
import gr.hua.dit.studyrooms.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST controller for weather-related API endpoints.
 * Provides endpoints to fetch current weather and weather forecasts
 * for specified geographic coordinates using the Open-Meteo weather service.
 * 
 * Base URL: /api/weather
 */
@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather", description = "Weather lookup via Open-Meteo")
public class WeatherApiController {

    // Service dependency for handling weather data retrieval logic
    private final WeatherService weatherService;

    /**
     * Constructor that initializes the controller with the WeatherService dependency.
     * Uses constructor injection for better testability and dependency management.
     * 
     * @param weatherService the service responsible for fetching weather data
     */
    public WeatherApiController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Retrieves the current weather for the specified geographic coordinates.
     * 
     * HTTP Method: GET
     * Endpoint: GET /api/weather?lat={latitude}&lon={longitude}
     * 
     * @param latitude the latitude coordinate (defaults to Harokopio University if omitted)
     * @param longitude the longitude coordinate (defaults to Harokopio University if omitted)
     * @return ResponseEntity containing WeatherDto with current weather data
     * 
     * Example: GET /api/weather?lat=37.9629&lon=23.7046
     */
    @Operation(summary = "Get current weather for coordinates")
    @GetMapping
    public ResponseEntity<WeatherDto> getCurrentWeather(
            @RequestParam(value = "lat", defaultValue = "37.9629") double latitude,
            @RequestParam(value = "lon", defaultValue = "23.7046") double longitude) {
        return ResponseEntity.ok(weatherService.getCurrentWeather(latitude, longitude));
    }

    /**
     * Retrieves forecasted weather for the specified coordinates at a specific point in time.
     * This endpoint requires all three parameters: latitude, longitude, and a timestamp.
     * 
     * HTTP Method: GET
     * Endpoint: GET /api/weather?lat={latitude}&lon={longitude}&at={datetime}
     * 
     * @param latitude the latitude coordinate (defaults to Harokopio University if omitted)
     * @param longitude the longitude coordinate (defaults to Harokopio University if omitted)
     * @param at the specific date and time for the weather forecast in ISO 8601 format
     *           (required parameter, format: YYYY-MM-DDTHH:mm:ss)
     * @return ResponseEntity containing WeatherDto with forecasted weather data for the specified time
     * 
     * Example: GET /api/weather?lat=37.9629&lon=23.7046&at=2025-01-09T14:30:00
     */
    @Operation(summary = "Get forecasted weather for coordinates at a specific time")
    @GetMapping(params = {"at"})
    public ResponseEntity<WeatherDto> getWeatherAt(
            @RequestParam(value = "lat", defaultValue = "37.9629") double latitude,
            @RequestParam(value = "lon", defaultValue = "23.7046") double longitude,
            @RequestParam("at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at) {
        return ResponseEntity.ok(weatherService.getWeather(latitude, longitude, at));
    }
}
