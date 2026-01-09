
// Package declaration
package gr.hua.dit.studyrooms.dto;


// Import Swagger annotation for API documentation
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * DTO for sending JWT token as a login response.
 * Used in API responses after successful authentication.
 */
@Schema(description = "JWT token response")
public class LoginResponseDto {


    /**
     * Bearer token that must be supplied in Authorization header.
     * Example value is shown for documentation purposes.
     */
    @Schema(description = "Bearer token that must be supplied in Authorization header", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;


    // Default constructor (required for serialization/deserialization)
    public LoginResponseDto() {
    }


    // Constructor to initialize with a token value
    public LoginResponseDto(String token) {
        this.token = token;
    }


    // Getter for the token field
    public String getToken() {
        return token;
    }


    // Setter for the token field
    public void setToken(String token) {
        this.token = token;
    }
}
