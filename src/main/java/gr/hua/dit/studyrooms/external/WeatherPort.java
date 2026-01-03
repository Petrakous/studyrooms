package gr.hua.dit.studyrooms.external;

import gr.hua.dit.studyrooms.dto.WeatherDto;
import java.time.LocalDateTime;

public interface WeatherPort {

    WeatherDto getCurrentWeather(double latitude, double longitude);

    WeatherDto getWeatherAt(double latitude, double longitude, LocalDateTime at);
}
