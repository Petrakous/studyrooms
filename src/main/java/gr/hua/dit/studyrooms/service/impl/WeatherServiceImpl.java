package gr.hua.dit.studyrooms.service.impl;

import gr.hua.dit.studyrooms.dto.WeatherDto;
import gr.hua.dit.studyrooms.external.WeatherPort;
import gr.hua.dit.studyrooms.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(45);

    private final WeatherPort weatherPort;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Duration ttl;

    @Autowired
    public WeatherServiceImpl(WeatherPort weatherPort) {
        this(weatherPort, DEFAULT_TTL);
    }

    WeatherServiceImpl(WeatherPort weatherPort, Duration ttl) {
        this.weatherPort = weatherPort;
        this.ttl = ttl;
    }

    @Override
    public WeatherDto getCurrentWeather(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);

        String cacheKey = latitude + "," + longitude;
        CacheEntry existing = cache.get(cacheKey);
        Instant now = Instant.now();
        if (existing != null && now.isBefore(existing.expiresAt)) {
            return existing.value;
        }

        WeatherDto fresh = weatherPort.getCurrentWeather(latitude, longitude);
        cache.put(cacheKey, new CacheEntry(fresh, now.plus(ttl)));
        return fresh;
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }

    private static class CacheEntry {
        private final WeatherDto value;
        private final Instant expiresAt;

        private CacheEntry(WeatherDto value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}
