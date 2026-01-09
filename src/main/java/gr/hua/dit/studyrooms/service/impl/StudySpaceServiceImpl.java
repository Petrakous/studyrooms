package gr.hua.dit.studyrooms.service.impl;

import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.repository.ReservationRepository;
import gr.hua.dit.studyrooms.repository.StudySpaceRepository;
import gr.hua.dit.studyrooms.service.StudySpaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalTime;


// Service implementation for managing StudySpace entities
@Service
@Transactional
public class StudySpaceServiceImpl implements StudySpaceService {


    // Repository for StudySpace entity database operations
    private final StudySpaceRepository studySpaceRepository;
    private final ReservationRepository reservationRepository;


    // Constructor injection of the repository
    public StudySpaceServiceImpl(StudySpaceRepository studySpaceRepository,
                                 ReservationRepository reservationRepository) {
        this.studySpaceRepository = studySpaceRepository;
        this.reservationRepository = reservationRepository;
    }


    /**
     * Retrieve all study spaces, ordered by name ascending.
     */
    @Override
    public List<StudySpace> getAllSpaces() {
        return studySpaceRepository.findAllByOrderByNameAsc();
    }


    /**
     * Get a study space by its ID. Throws IllegalArgumentException if not found.
     */
    @Override
    public StudySpace getSpaceById(Long id) {
        return studySpaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("StudySpace not found: " + id));
    }


    /**
     * Create and save a new study space.
     */
    @Override
    public StudySpace createSpace(StudySpace space) {
        validateOperatingHours(space);
        return studySpaceRepository.save(space);
    }


    /**
     * Update an existing study space's details by ID.
     * Copies fields from the updated object to the existing one.
     */
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

    /**
     * Delete a study space by its ID.
     */
    @Override
    public void deleteSpace(Long id) {
        reservationRepository.deleteByStudySpaceId(id);
        studySpaceRepository.deleteById(id);
    }

    private void validateOperatingHours(StudySpace space) {
        if (space == null) {
            return;
        }
        if (space.isFullDay()) {
            if (space.getOpenTime() == null) {
                space.setOpenTime(LocalTime.MIDNIGHT);
            }
            if (space.getCloseTime() == null) {
                space.setCloseTime(LocalTime.of(23, 59));
            }
            return;
        }
        if (space.getOpenTime() == null || space.getCloseTime() == null) {
            throw new IllegalArgumentException("Open and close time are required unless Full day is selected.");
        }
        if (!space.isFullDay()
                && space.getOpenTime() != null
                && space.getCloseTime() != null
                && !space.getCloseTime().isAfter(space.getOpenTime())) {
            throw new IllegalArgumentException("Close time must be after open time unless Full day is selected.");
        }
    }
}
