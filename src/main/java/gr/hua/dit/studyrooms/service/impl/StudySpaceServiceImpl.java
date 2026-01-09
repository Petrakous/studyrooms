package gr.hua.dit.studyrooms.service.impl;

import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.repository.StudySpaceRepository;
import gr.hua.dit.studyrooms.service.StudySpaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


// Service implementation for managing StudySpace entities
@Service
@Transactional
public class StudySpaceServiceImpl implements StudySpaceService {


    // Repository for StudySpace entity database operations
    private final StudySpaceRepository studySpaceRepository;


    // Constructor injection of the repository
    public StudySpaceServiceImpl(StudySpaceRepository studySpaceRepository) {
        this.studySpaceRepository = studySpaceRepository;
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
        return studySpaceRepository.save(space);
    }


    /**
     * Update an existing study space's details by ID.
     * Copies fields from the updated object to the existing one.
     */
    @Override
    public StudySpace updateSpace(Long id, StudySpace updated) {
        StudySpace existing = getSpaceById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setCapacity(updated.getCapacity());
        existing.setOpenTime(updated.getOpenTime());
        existing.setCloseTime(updated.getCloseTime());
        return studySpaceRepository.save(existing);
    }

    /**
     * Delete a study space by its ID.
     */
    @Override
    public void deleteSpace(Long id) {
        studySpaceRepository.deleteById(id);
    }
}