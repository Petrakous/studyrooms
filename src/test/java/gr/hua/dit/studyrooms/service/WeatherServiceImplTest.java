package gr.hua.dit.studyrooms.service.impl;

import gr.hua.dit.studyrooms.dto.WeatherDto;
import gr.hua.dit.studyrooms.external.WeatherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private WeatherPort weatherPort;

    private WeatherServiceImpl weatherService;

    @BeforeEach
    void setup() {
        weatherService = new WeatherServiceImpl(weatherPort, Duration.ofMinutes(30));
    }

    @Test
    void rejectsOutOfBoundsLatitude() {
        assertThrows(IllegalArgumentException.class, () -> weatherService.getCurrentWeather(91, 10));
    }

    @Test
    void rejectsOutOfBoundsLongitude() {
        assertThrows(IllegalArgumentException.class, () -> weatherService.getCurrentWeather(10, -181));
    }

    @Test
    void returnsCachedResultWithinTtl() {
        WeatherDto dto = new WeatherDto();
        when(weatherPort.getCurrentWeather(38.0, 23.0)).thenReturn(dto);

        WeatherDto first = weatherService.getCurrentWeather(38.0, 23.0);
        WeatherDto second = weatherService.getCurrentWeather(38.0, 23.0);

        assertSame(first, second);
        verify(weatherPort, times(1)).getCurrentWeather(38.0, 23.0);
    }
}
