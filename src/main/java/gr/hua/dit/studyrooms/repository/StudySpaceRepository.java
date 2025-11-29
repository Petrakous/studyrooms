package gr.hua.dit.studyrooms.repository;

import gr.hua.dit.studyrooms.entity.StudySpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalTime;

public interface StudySpaceRepository extends JpaRepository<StudySpace, Long> {

    // για εμφάνιση λίστας χώρων με αλφαβητική σειρά
    List<StudySpace> findAllByOrderByNameAsc();

    // πόσοι χώροι είναι ανοιχτοί τώρα (openTime <= now < closeTime)
    long countByOpenTimeLessThanEqualAndCloseTimeGreaterThan(LocalTime openTime, LocalTime closeTime);
}
