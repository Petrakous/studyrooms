package gr.hua.dit.studyrooms.repository;

import gr.hua.dit.studyrooms.entity.StudySpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalTime;

public interface StudySpaceRepository extends JpaRepository<StudySpace, Long> {

    // για εμφάνιση λίστας χώρων με αλφαβητική σειρά
    List<StudySpace> findAllByOrderByNameAsc();

    // πόσοι χώροι είναι ανοιχτοί τώρα (fullDay ή openTime <= now < closeTime)
    @Query("""
            select count(s)
            from StudySpace s
            where s.fullDay = true
               or (s.openTime <= :now and s.closeTime > :now)
            """)
    long countOpenNow(@Param("now") LocalTime now);

    Optional<StudySpace> findByName(String name);
}
