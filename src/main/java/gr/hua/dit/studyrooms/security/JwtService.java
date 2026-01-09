package gr.hua.dit.studyrooms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;


// Service for handling JWT operations: creation, parsing, and validation
@Service
public class JwtService {


    // Secret key for signing JWTs (injected from application properties)
    private final String secret;
    // Token expiration time in milliseconds (injected from application properties)
    private final long expirationMs;


    // Constructor with injected secret and expiration values
    public JwtService(
            @Value("${studyrooms.jwt.secret}") String secret,
            @Value("${studyrooms.jwt.expiration-ms}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }


    // Returns the cryptographic key used to sign and verify JWTs
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    // Extracts the username (subject) from the JWT
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    // Extracts a specific claim from the JWT using a resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    // Parses the JWT and returns all claims (payload data)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    // Generates a JWT for the given user without extra claims
    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }


    // Generates a JWT for the given user, including any extra claims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // set the username as the subject
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }


    // Validates the token: checks username matches and token is not expired
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }


    // Checks if the token's expiration date is before the current time
    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
}
