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

/**
 * REST API Controller for managing study spaces.
 * 
 * Provides endpoints for CRUD operations on study spaces with role-based access control.
 * All endpoints require bearer token authentication.
 * Write operations (POST, PUT, DELETE) are restricted to users with STAFF role.
 * 
 * Base URL: /api/spaces
 */
@RestController
@RequestMapping("/api/spaces")
@Tag(name = "Spaces", description = "Study space management")
@SecurityRequirement(name = "bearerAuth")
public class StudySpaceApiController {

    // Service layer dependency for business logic operations
    private final StudySpaceService studySpaceService;

    /**
     * Constructor with dependency injection.
     * 
     * @param studySpaceService the service layer for study space operations
     */
    public StudySpaceApiController(StudySpaceService studySpaceService) {
        this.studySpaceService = studySpaceService;
    }

    /**
     * Retrieves all available study spaces.
     * 
     * Public endpoint - no role restrictions.
     * HTTP Method: GET
     * Endpoint: GET /api/spaces
     * 
     * @return a ResponseEntity containing a list of all study spaces
     */
    @Operation(summary = "List all study spaces")
    @GetMapping
    public ResponseEntity<List<StudySpace>> getAllSpaces() {
        return ResponseEntity.ok(studySpaceService.getAllSpaces());
    }

    /**
     * Retrieves a specific study space by its ID.
     * 
     * Public endpoint - no role restrictions.
     * HTTP Method: GET
     * Endpoint: GET /api/spaces/{id}
     * 
     * @param id the unique identifier of the study space to retrieve
     * @return a ResponseEntity containing the requested study space
     */
    @Operation(summary = "Get a study space by id")
    @GetMapping("/{id}")
    public ResponseEntity<StudySpace> getSpace(@PathVariable Long id) {
        return ResponseEntity.ok(studySpaceService.getSpaceById(id));
    }

    /**
     * Creates a new study space.
     * 
     * Restricted to users with STAFF role only.
     * HTTP Method: POST
     * Endpoint: POST /api/spaces
     * Request body must contain valid StudySpaceDto with required fields.
     * 
     * @param space a valid StudySpaceDto object containing the space details
     * @return a ResponseEntity containing the newly created study space with auto-generated ID
     */
    @Operation(summary = "Create a new study space (staff only)")
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF')")
    public ResponseEntity<StudySpace> createSpace(@Valid @RequestBody StudySpaceDto space) {
        return ResponseEntity.ok(studySpaceService.createSpace(StudySpaceMapper.toEntity(space)));
    }

    /**
     * Updates an existing study space.
     * 
     * Restricted to users with STAFF role only.
     * HTTP Method: PUT
     * Endpoint: PUT /api/spaces/{id}
     * Request body must contain valid StudySpaceDto with fields to update.
     * 
     * @param id the unique identifier of the space to update
     * @param space a valid StudySpaceDto object containing the updated space details
     * @return a ResponseEntity containing the updated study space
     */
    @Operation(summary = "Update a study space (staff only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF')")
    public ResponseEntity<StudySpace> updateSpace(@PathVariable Long id,
                                                  @Valid @RequestBody StudySpaceDto space) {
        return ResponseEntity.ok(studySpaceService.updateSpace(id, StudySpaceMapper.toEntity(space)));
    }

    /**
     * Deletes a study space.
     * 
     * Restricted to users with STAFF role only.
     * HTTP Method: DELETE
     * Endpoint: DELETE /api/spaces/{id}
     * Returns 204 No Content on successful deletion.
     * 
     * @param id the unique identifier of the space to delete
     * @return a ResponseEntity with no content status (204)
     */
    @Operation(summary = "Delete a study space (staff only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF')")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        studySpaceService.deleteSpace(id);
        return ResponseEntity.noContent().build();
    }

}
