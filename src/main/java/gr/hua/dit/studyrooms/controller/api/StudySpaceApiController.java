package gr.hua.dit.studyrooms.controller.api;

import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.service.StudySpaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
public class StudySpaceApiController {

    private final StudySpaceService studySpaceService;

    public StudySpaceApiController(StudySpaceService studySpaceService) {
        this.studySpaceService = studySpaceService;
    }

    // GET /api/spaces
    @GetMapping
    public ResponseEntity<List<StudySpace>> getAllSpaces() {
        return ResponseEntity.ok(studySpaceService.getAllSpaces());
    }

    // GET /api/spaces/{id}
    @GetMapping("/{id}")
    public ResponseEntity<StudySpace> getSpace(@PathVariable Long id) {
        return ResponseEntity.ok(studySpaceService.getSpaceById(id));
    }

    // POST /api/spaces (STAFF)
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF')")
    public ResponseEntity<StudySpace> createSpace(@RequestBody StudySpace space) {
        return ResponseEntity.ok(studySpaceService.createSpace(space));
    }

    // PUT /api/spaces/{id} (STAFF)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF')")
    public ResponseEntity<StudySpace> updateSpace(@PathVariable Long id,
                                                  @RequestBody StudySpace space) {
        return ResponseEntity.ok(studySpaceService.updateSpace(id, space));
    }

    // DELETE /api/spaces/{id} (STAFF)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF')")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        studySpaceService.deleteSpace(id);
        return ResponseEntity.noContent().build();
    }
}
