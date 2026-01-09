package gr.hua.dit.studyrooms.service.impl;


import gr.hua.dit.studyrooms.dto.WeatherDto;
import gr.hua.dit.studyrooms.external.WeatherPort;
import gr.hua.dit.studyrooms.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeatherServiceImpl implements WeatherService {

    // Default time-to-live for cache entries (45 minutes)
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(45);

    // Port to fetch weather data from external source
    private final WeatherPort weatherPort;
    // Thread-safe cache for storing weather data
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    // Time-to-live for cache entries
    private final Duration ttl;

    /**
     * Constructor used by Spring, sets default TTL.
     * @param weatherPort the port to fetch weather data
     */
    @Autowired
    public WeatherServiceImpl(WeatherPort weatherPort) {
        this(weatherPort, DEFAULT_TTL);
    }

    /**
     * Constructor with custom TTL (mainly for testing).
     * @param weatherPort the port to fetch weather data
     * @param ttl cache time-to-live
     */
    public WeatherServiceImpl(WeatherPort weatherPort, Duration ttl) {
        this.weatherPort = weatherPort;
        this.ttl = ttl;
    }

    /**
     * Get current weather for given coordinates.
     * @param latitude latitude
     * @param longitude longitude
     * @return WeatherDto for current time
     */
    @Override
    public WeatherDto getCurrentWeather(double latitude, double longitude) {
        // Delegate to getWeather with null time (current)
        return getWeather(latitude, longitude, null);
    }

    /**
     * Get weather for given coordinates and time (rounded to hour).
     * Uses cache if available and not expired.
     * @param latitude latitude
     * @param longitude longitude
     * @param at time to get weather for (null = now)
     * @return WeatherDto for requested time
     */
    @Override
    public WeatherDto getWeather(double latitude, double longitude, LocalDateTime at) {
        validateCoordinates(latitude, longitude);

        // Normalize time to hour for caching
        LocalDateTime normalizedAt = at != null ? at.truncatedTo(ChronoUnit.HOURS) : null;
        // Build cache key based on coordinates and time
        String cacheKey = latitude + "," + longitude + "," + (normalizedAt != null ? normalizedAt : "now");
        CacheEntry existing = cache.get(cacheKey);
        Instant now = Instant.now();
        // Return cached value if present and not expired
        if (existing != null && now.isBefore(existing.expiresAt)) {
            return existing.value;
        }

        // Fetch fresh data from external port
        WeatherDto fresh = normalizedAt == null
                ? weatherPort.getCurrentWeather(latitude, longitude)
                : weatherPort.getWeatherAt(latitude, longitude, normalizedAt);
        // Store in cache with new expiry
        cache.put(cacheKey, new CacheEntry(fresh, now.plus(ttl)));
        return fresh;
    }

    /**
     * Validates latitude and longitude values.
     * @param latitude latitude
     * @param longitude longitude
     * @throws IllegalArgumentException if out of bounds
     */
    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }

    /**
     * Simple cache entry holding value and expiry time.
     */
    private static class CacheEntry {
        private final WeatherDto value;
        private final Instant expiresAt;

        private CacheEntry(WeatherDto value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}
