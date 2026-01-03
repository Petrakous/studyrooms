package gr.hua.dit.studyrooms.external;

import gr.hua.dit.studyrooms.dto.WeatherDto;

public interface WeatherPort {

    WeatherDto getCurrentWeather(double latitude, double longitude);
}
