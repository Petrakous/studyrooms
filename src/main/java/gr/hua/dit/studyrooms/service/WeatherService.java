package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.dto.WeatherDto;
import java.time.LocalDateTime;

public interface WeatherService {

    WeatherDto getCurrentWeather(double latitude, double longitude);

    WeatherDto getWeather(double latitude, double longitude, LocalDateTime at);
}
