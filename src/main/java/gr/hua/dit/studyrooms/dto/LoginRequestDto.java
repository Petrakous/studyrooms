package gr.hua.dit.studyrooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) for login requests.
 * Contains username and password fields with validation and API documentation annotations.
 */
@Schema(description = "Credentials used for username/password authentication")
public class LoginRequestDto {
    // Username for authentication
    @Schema(description = "Username", example = "student1")
    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must be at most 50 characters")
    private String username;

    // Password for authentication
    @Schema(description = "Password", example = "secret")
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    // Default constructor
    public LoginRequestDto() {
    }

    /**
     * Gets the username.
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
