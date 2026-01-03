package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.dto.WeatherDto;

public interface WeatherService {

    WeatherDto getCurrentWeather(double latitude, double longitude);
}
