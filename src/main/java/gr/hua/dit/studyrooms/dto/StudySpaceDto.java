// DTO for transferring study space data between layers (API, forms, etc.)
package gr.hua.dit.studyrooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Schema(description = "Study space payload used by forms and API requests")
/**
 * Data Transfer Object representing a study space, used for API requests and forms.
 */
public class StudySpaceDto {

    @Schema(description = "Identifier used for updates", example = "1")
    /**
     * Unique identifier for the study space (used for updates).
     */
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 80, message = "Name must be at most 80 characters")
    @Schema(description = "Name of the space", example = "Library Room A")
    /**
     * Name of the study space. Required, max 80 characters.
     */
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    @Schema(description = "Optional description of the space", example = "Quiet study area with 20 seats")
    /**
     * Optional description of the study space. Max 500 characters.
     */
    private String description;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    @Schema(description = "Maximum number of users", example = "20")
    /**
     * Maximum number of users allowed in the space. Must be positive.
     */
    private Integer capacity;

    @NotNull(message = "Open time is required")
    @Schema(description = "Opening time", example = "08:00:00")
    /**
     * Opening time of the study space (e.g., 08:00:00). Required.
     */
    private LocalTime openTime;

    @NotNull(message = "Close time is required")
    @Schema(description = "Closing time", example = "20:00:00")
    /**
     * Closing time of the study space (e.g., 20:00:00). Required.
     */
    private LocalTime closeTime;

    /**
     * Gets the unique identifier of the study space.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the study space.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name of the study space.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the study space.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the study space.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the study space.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the capacity of the study space.
     */
    public Integer getCapacity() {
        return capacity;
    }

    /**
     * Sets the capacity of the study space.
     */
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    /**
     * Gets the opening time of the study space.
     */
    public LocalTime getOpenTime() {
        return openTime;
    }

    /**
     * Sets the opening time of the study space.
     */
    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    /**
     * Gets the closing time of the study space.
     */
    public LocalTime getCloseTime() {
        return closeTime;
    }

    /**
     * Sets the closing time of the study space.
     */
    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }
}
