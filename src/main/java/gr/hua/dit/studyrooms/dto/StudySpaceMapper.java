package gr.hua.dit.studyrooms.dto;

import gr.hua.dit.studyrooms.entity.StudySpace;

/**
 * Utility methods for converting study spaces between DTOs and entities.
 */
public final class StudySpaceMapper {

    private StudySpaceMapper() {
    }

    public static StudySpace toEntity(StudySpaceDto dto) {
        if (dto == null) {
            return null;
        }
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

    public static StudySpaceDto toDto(StudySpace space) {
        if (space == null) {
            return null;
        }
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
