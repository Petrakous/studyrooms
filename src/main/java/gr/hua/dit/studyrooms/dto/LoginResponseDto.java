package gr.hua.dit.studyrooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT token response")
public class LoginResponseDto {

    @Schema(description = "Bearer token that must be supplied in Authorization header", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
