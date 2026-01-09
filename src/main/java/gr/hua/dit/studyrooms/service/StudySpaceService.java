
// Service interface for managing StudySpace entities (CRUD operations)
package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.entity.StudySpace;

import java.util.List;


/**
 * Service interface for managing StudySpace entities.
 * Defines CRUD operations for StudySpace objects.
 */
public interface StudySpaceService {


    /**
     * Retrieve all study spaces.
     * @return List of all StudySpace entities
     */
    List<StudySpace> getAllSpaces();


    /**
     * Retrieve a study space by its unique ID.
     * @param id the ID of the study space
     * @return the StudySpace entity, or null if not found
     */
    StudySpace getSpaceById(Long id);


    /**
     * Create a new study space.
     * @param space the StudySpace entity to create
     * @return the created StudySpace entity
     */
    StudySpace createSpace(StudySpace space);


    /**
     * Update an existing study space.
     * @param id the ID of the study space to update
     * @param updated the updated StudySpace entity
     * @return the updated StudySpace entity
     */
    StudySpace updateSpace(Long id, StudySpace updated);


    /**
     * Delete a study space by its ID.
     * @param id the ID of the study space to delete
     */
    void deleteSpace(Long id);
}
