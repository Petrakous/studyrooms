package gr.hua.dit.studyrooms.repository;

import gr.hua.dit.studyrooms.entity.StudySpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Spaces open now (fullDay or openTime <= now < closeTime)
    @Query("""
            select count(s)
            from StudySpace s
            where s.fullDay = true
               or (s.openTime <= :now and s.closeTime > :now)
            """)
    long countOpenNow(@Param("now") LocalTime now);


    /**
     * Finds a StudySpace entity by its name.
     * @param name The name of the StudySpace
     * @return Optional containing the StudySpace if found, or empty if not
     */
    Optional<StudySpace> findByName(String name);
}
