package gr.hua.dit.studyrooms.repository;

import gr.hua.dit.studyrooms.entity.StudySpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalTime;

/**
 * Repository interface for StudySpace entity.
 * Extends JpaRepository to provide CRUD operations and custom queries.
 */
public interface StudySpaceRepository extends JpaRepository<StudySpace, Long> {


    /**
     * Returns a list of all StudySpace entities sorted alphabetically by name.
     * @return List of StudySpace sorted by name ascending
     */
    List<StudySpace> findAllByOrderByNameAsc();


    /**
     * Counts the number of StudySpace entities that are currently open.
     * A space is considered open if openTime <= now < closeTime.
     * @param openTime The current time to compare with openTime
     * @param closeTime The current time to compare with closeTime
     * @return Number of open StudySpace entities
     */
    long countByOpenTimeLessThanEqualAndCloseTimeGreaterThan(LocalTime openTime, LocalTime closeTime);


    /**
     * Finds a StudySpace entity by its name.
     * @param name The name of the StudySpace
     * @return Optional containing the StudySpace if found, or empty if not
     */
    Optional<StudySpace> findByName(String name);
}
