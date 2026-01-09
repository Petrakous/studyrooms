package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.dto.WeatherDto;
import gr.hua.dit.studyrooms.external.WeatherPort;
import gr.hua.dit.studyrooms.service.impl.WeatherServiceImpl;
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

/**
 * Unit tests for {@link WeatherServiceImpl}.
 * 
 * Tests cover:
 * - Input validation for geographic coordinates (latitude/longitude bounds)
 * - Caching behavior to avoid redundant external API calls
 * 
 * Uses Mockito to mock the external {@link WeatherPort} dependency.
 */
@ExtendWith(MockitoExtension.class)  // Enables Mockito annotations like @Mock
class WeatherServiceImplTest {

    /**
     * Mock of the external weather API port.
     * This avoids making real HTTP calls during tests.
     */
    @Mock
    private WeatherPort weatherPort;

    /** The service under test */
    private WeatherServiceImpl weatherService;

    /**
     * Runs before each test method.
     * Creates a fresh WeatherServiceImpl instance with a 30-minute cache TTL.
     */
    @BeforeEach
    void setup() {
        weatherService = new WeatherServiceImpl(weatherPort, Duration.ofMinutes(30));
    }

    /**
     * Verifies that latitude values outside the valid range [-90, 90] are rejected.
     * Latitude 91 is invalid and should throw IllegalArgumentException.
     */
    @Test
    void rejectsOutOfBoundsLatitude() {
        assertThrows(IllegalArgumentException.class, () -> weatherService.getCurrentWeather(91, 10));
    }

    /**
     * Verifies that longitude values outside the valid range [-180, 180] are rejected.
     * Longitude -181 is invalid and should throw IllegalArgumentException.
     */
    @Test
    void rejectsOutOfBoundsLongitude() {
        assertThrows(IllegalArgumentException.class, () -> weatherService.getCurrentWeather(10, -181));
    }

    /**
     * Verifies that the weather service caches results properly.
     * 
     * When the same coordinates are requested twice within the TTL (30 min),
     * the service should:
     * 1. Return the exact same cached object (assertSame)
     * 2. Only call the external API once (verify times(1))
     * 
     * This test uses Athens, Greece coordinates (38.0, 23.0) as sample data.
     */
    @Test
    void returnsCachedResultWithinTtl() {
        // Arrange: Configure mock to return a DTO when called with Athens coordinates
        WeatherDto dto = new WeatherDto();
        when(weatherPort.getCurrentWeather(38.0, 23.0)).thenReturn(dto);

        // Act: Call the service twice with the same coordinates
        WeatherDto first = weatherService.getCurrentWeather(38.0, 23.0);
        WeatherDto second = weatherService.getCurrentWeather(38.0, 23.0);

        // Assert: Both calls should return the same cached object
        assertSame(first, second);
        // Assert: The external API should only be called once (cached on second call)
        verify(weatherPort, times(1)).getCurrentWeather(38.0, 23.0);
    }
}
