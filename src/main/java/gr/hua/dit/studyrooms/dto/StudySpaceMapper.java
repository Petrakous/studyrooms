
// Package declaration for the DTO (Data Transfer Object) layer
package gr.hua.dit.studyrooms.dto;


// Import the StudySpace entity class
import gr.hua.dit.studyrooms.entity.StudySpace;

/**
 * Utility class for converting between StudySpace entity and StudySpaceDto.
 * This helps separate the internal data model from the data exposed to clients.
 */
public final class StudySpaceMapper {

    // Private constructor to prevent instantiation of this utility class
    private StudySpaceMapper() {
    }

    /**
     * Converts a StudySpaceDto object to a StudySpace entity.
     * Copies all relevant fields from the DTO to a new entity instance.
     *
     * @param dto the StudySpaceDto to convert
     * @return a new StudySpace entity with fields copied from the DTO, or null if dto is null
     */
    public static StudySpace toEntity(StudySpaceDto dto) {
        if (dto == null) {
            // Return null if the input DTO is null
            return null;
        }
        // Create a new StudySpace entity and set its fields from the DTO
        StudySpace space = new StudySpace();
        space.setId(dto.getId());
        space.setName(dto.getName());
        space.setDescription(dto.getDescription());
        space.setCapacity(dto.getCapacity());
        space.setOpenTime(dto.getOpenTime());
        space.setCloseTime(dto.getCloseTime());
        space.setFullDay(dto.isFullDay());
        return space;
    }

    /**
     * Converts a StudySpace entity to a StudySpaceDto object.
     * Copies all relevant fields from the entity to a new DTO instance.
     *
     * @param space the StudySpace entity to convert
     * @return a new StudySpaceDto with fields copied from the entity, or null if space is null
     */
    public static StudySpaceDto toDto(StudySpace space) {
        if (space == null) {
            // Return null if the input entity is null
            return null;
        }
        // Create a new StudySpaceDto and set its fields from the entity
        StudySpaceDto dto = new StudySpaceDto();
        dto.setId(space.getId());
        dto.setName(space.getName());
        dto.setDescription(space.getDescription());
        dto.setCapacity(space.getCapacity());
        dto.setOpenTime(space.getOpenTime());
        dto.setCloseTime(space.getCloseTime());
        dto.setFullDay(space.isFullDay());
        return dto;
    }
}
