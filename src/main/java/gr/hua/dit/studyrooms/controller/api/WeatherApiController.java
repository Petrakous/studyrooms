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

@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather", description = "Weather lookup via Open-Meteo")
public class WeatherApiController {

    private final WeatherService weatherService;

    public WeatherApiController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Operation(summary = "Get current weather for coordinates")
    @GetMapping
    public ResponseEntity<WeatherDto> getCurrentWeather(@RequestParam("lat") double latitude,
                                                        @RequestParam("lon") double longitude) {
        return ResponseEntity.ok(weatherService.getCurrentWeather(latitude, longitude));
    }

    @Operation(summary = "Get forecasted weather for coordinates at a specific time")
    @GetMapping(params = {"lat", "lon", "at"})
    public ResponseEntity<WeatherDto> getWeatherAt(
            @RequestParam("lat") double latitude,
            @RequestParam("lon") double longitude,
            @RequestParam("at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at) {
        return ResponseEntity.ok(weatherService.getWeather(latitude, longitude, at));
    }
}
