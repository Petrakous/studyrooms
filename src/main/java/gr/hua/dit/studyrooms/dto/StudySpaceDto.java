package gr.hua.dit.studyrooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Schema(description = "Study space payload used by forms and API requests")
public class StudySpaceDto {

    @Schema(description = "Identifier used for updates", example = "1")
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 80, message = "Name must be at most 80 characters")
    @Schema(description = "Name of the space", example = "Library Room A")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    @Schema(description = "Optional description of the space", example = "Quiet study area with 20 seats")
    private String description;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    @Schema(description = "Maximum number of users", example = "20")
    private Integer capacity;

    @NotNull(message = "Open time is required")
    @Schema(description = "Opening time", example = "08:00:00")
    private LocalTime openTime;

    @NotNull(message = "Close time is required")
    @Schema(description = "Closing time", example = "20:00:00")
    private LocalTime closeTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }
}
