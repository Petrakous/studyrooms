package gr.hua.dit.studyrooms.service.impl;

import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.repository.StudySpaceRepository;
import gr.hua.dit.studyrooms.service.StudySpaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudySpaceServiceImpl implements StudySpaceService {

    private final StudySpaceRepository studySpaceRepository;

    public StudySpaceServiceImpl(StudySpaceRepository studySpaceRepository) {
        this.studySpaceRepository = studySpaceRepository;
    }

    @Override
    public List<StudySpace> getAllSpaces() {
        return studySpaceRepository.findAllByOrderByNameAsc();
    }

    @Override
    public StudySpace getSpaceById(Long id) {
        return studySpaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("StudySpace not found: " + id));
    }

    @Override
    public StudySpace createSpace(StudySpace space) {
        validateOperatingHours(space);
        return studySpaceRepository.save(space);
    }

    @Override
    public StudySpace updateSpace(Long id, StudySpace updated) {
        validateOperatingHours(updated);
        StudySpace existing = getSpaceById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setCapacity(updated.getCapacity());
        existing.setOpenTime(updated.getOpenTime());
        existing.setCloseTime(updated.getCloseTime());
        existing.setFullDay(updated.isFullDay());
        return studySpaceRepository.save(existing);
    }

    @Override
    public void deleteSpace(Long id) {
        studySpaceRepository.deleteById(id);
    }

    private void validateOperatingHours(StudySpace space) {
        if (space == null) {
            return;
        }
        if (!space.isFullDay()
                && space.getOpenTime() != null
                && space.getCloseTime() != null
                && !space.getCloseTime().isAfter(space.getOpenTime())) {
            throw new IllegalArgumentException("Close time must be after open time unless Full day is selected.");
        }
    }
}
