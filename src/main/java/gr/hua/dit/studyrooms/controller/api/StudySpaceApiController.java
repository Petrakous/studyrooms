package gr.hua.dit.studyrooms.controller.api;

import gr.hua.dit.studyrooms.dto.StudySpaceDto;
import gr.hua.dit.studyrooms.dto.StudySpaceMapper;
import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.service.StudySpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
@Tag(name = "Spaces", description = "Study space management")
@SecurityRequirement(name = "bearerAuth")
public class StudySpaceApiController {

    private final StudySpaceService studySpaceService;

    public StudySpaceApiController(StudySpaceService studySpaceService) {
        this.studySpaceService = studySpaceService;
    }

    // GET /api/spaces
    @Operation(summary = "List all study spaces")
    @GetMapping
    public ResponseEntity<List<StudySpace>> getAllSpaces() {
        return ResponseEntity.ok(studySpaceService.getAllSpaces());
    }

    // GET /api/spaces/{id}
    @Operation(summary = "Get a study space by id")
    @GetMapping("/{id}")
    public ResponseEntity<StudySpace> getSpace(@PathVariable Long id) {
        return ResponseEntity.ok(studySpaceService.getSpaceById(id));
    }

    // POST /api/spaces (STAFF)
    @Operation(summary = "Create a new study space (staff only)")
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF')")
    public ResponseEntity<StudySpace> createSpace(@Valid @RequestBody StudySpaceDto space) {
        return ResponseEntity.ok(studySpaceService.createSpace(StudySpaceMapper.toEntity(space)));
    }

    // PUT /api/spaces/{id} (STAFF)
    @Operation(summary = "Update a study space (staff only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF')")
    public ResponseEntity<StudySpace> updateSpace(@PathVariable Long id,
                                                  @Valid @RequestBody StudySpaceDto space) {
        return ResponseEntity.ok(studySpaceService.updateSpace(id, StudySpaceMapper.toEntity(space)));
    }

    // DELETE /api/spaces/{id} (STAFF)
    @Operation(summary = "Delete a study space (staff only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF')")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        studySpaceService.deleteSpace(id);
        return ResponseEntity.noContent().build();
    }

}
