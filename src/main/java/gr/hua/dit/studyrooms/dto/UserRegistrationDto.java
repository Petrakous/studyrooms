package gr.hua.dit.studyrooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Registration payload for creating a new student account")
public class UserRegistrationDto {

    @Schema(description = "Unique username", example = "student1")
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Schema(description = "Password", example = "secret123")
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @Schema(description = "Password confirmation", example = "secret123")
    @NotBlank(message = "Confirm your password")
    private String confirmPassword;

    @Schema(description = "Full name of the student", example = "Student Name")
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;

    @Schema(description = "Email of the student", example = "student@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;

    public UserRegistrationDto() {
    }

    // full constructor
    public UserRegistrationDto(String username, String password, String confirmPassword,
                               String fullName, String email) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.fullName = fullName;
        this.email = email;
    }

    // getters & setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordsMatch() {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
}
