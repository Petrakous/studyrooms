package gr.hua.dit.studyrooms.controller.api;

import gr.hua.dit.studyrooms.external.ExternalServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API endpoints.
 * 
 * This class uses @RestControllerAdvice to intercept exceptions thrown by API controllers
 * and convert them into standardized JSON error responses with appropriate HTTP status codes.
 * 
 * The handler is scoped to specific controller classes (Auth, Reservation, StudySpace, etc.)
 * via basePackageClasses to avoid accidentally handling exceptions from other parts of the app.
 * 
 * Each @ExceptionHandler method catches a specific exception type and returns a ResponseEntity
 * with a JSON body containing error details and the corresponding HTTP status.
 */
@RestControllerAdvice(basePackageClasses = {
        AuthApiController.class,
        ReservationApiController.class,
        StudySpaceApiController.class,
        StaffApiController.class,
        StatsApiController.class,
        WeatherApiController.class
})
public class ApiExceptionHandler {

    /**
     * Handles request body validation failures.
     * 
     * This handler catches MethodArgumentNotValidException, which Spring throws when
     * @Valid or @Validated annotations fail (e.g., required fields missing, invalid format).
     * 
     * Response includes:
     * - "message": generic validation failure message
     * - "errors": map of field names to their specific validation error messages
     * 
     * HTTP Status: 400 BAD_REQUEST (client error in request format/content)
     * 
     * @param ex the validation exception thrown by Spring's data binding
     * @return ResponseEntity with 400 status and field-level error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Extract field-level validation errors from the binding result
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // Map each field name to its validation error message
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        // Build response body with overall message and detailed field errors
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Validation failed");
        body.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles business logic violations detected during request processing.
     * 
     * This handler catches IllegalArgumentException and IllegalStateException, which are
     * typically thrown when:
     * - Invalid parameters are passed to business logic
     * - Operation violates current state constraints (e.g., cannot cancel an already completed reservation)
     * 
     * HTTP Status: 400 BAD_REQUEST (client sent a request that violates business rules)
     * 
     * @param ex the exception containing the specific violation message
     * @return ResponseEntity with 400 status and the exception message
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        Map<String, String> body = new HashMap<>();
        // Include the exception message so clients understand what constraint was violated
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles authorization and authentication failures.
     * 
     * This handler catches SecurityException, which is thrown when:
     * - User lacks required permissions for an operation
     * - Authentication credentials are invalid or missing
     * - Access to a protected resource is denied
     * 
     * HTTP Status: 403 FORBIDDEN (client authenticated but not authorized for this resource)
     * 
     * @param ex the security exception with details about the access denial
     * @return ResponseEntity with 403 status and reason message
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurity(SecurityException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Handles failures in external third-party API calls.
     * 
     * This handler catches ExternalServiceException, which is thrown when calls to
     * external services fail (e.g., holiday API, weather API, notification service).
     * 
     * HTTP Status: 502 BAD_GATEWAY (indicates the upstream service is the problem,
     * not this server—useful for load balancers and monitoring).
     * 
     * @param ex the external service exception containing failure details
     * @return ResponseEntity with 502 status explaining the external service issue
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Map<String, String>> handleExternal(ExternalServiceException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
}

