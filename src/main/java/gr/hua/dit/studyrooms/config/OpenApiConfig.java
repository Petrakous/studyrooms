package gr.hua.dit.studyrooms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the StudyRooms application.
 *
 * This configuration customizes the generated OpenAPI metadata that appears in
 * Swagger UI and the OpenAPI JSON/YAML. It declares a global HTTP Bearer
 * authentication scheme named "bearerAuth" (JWT) so clients can supply a
 * bearer token when trying secured endpoints from the UI.
 *
 * Note: this class only affects documentation; it does not enforce security at
 * runtime. Actual enforcement must be implemented via Spring Security filters
 * and JWT validation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates the application-wide OpenAPI definition with title, version and
     * a default security requirement.
     *
     * @return configured OpenAPI instance used by Swagger/OpenAPI tooling
     */
    @Bean
    public OpenAPI studyRoomsOpenApi() {
        // Logical name for the security scheme shown in the Swagger UI
        final String securitySchemeName = "bearerAuth";

        // Build and return the OpenAPI descriptor. The returned object sets the
        // API information (title/version/description), declares that operations
        // require the bearer token, and registers the bearer security scheme in
        // the components section so UIs know how to collect the token.
        return new OpenAPI()
                .info(new Info()
                        .title("StudyRooms API")
                        .version("1.0")
                        .description("REST API for Study Rooms reservation system"))
                // Add a global security requirement so endpoints show as protected
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // Register the actual security scheme definition (HTTP Bearer/JWT)
                .components(new Components().addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

