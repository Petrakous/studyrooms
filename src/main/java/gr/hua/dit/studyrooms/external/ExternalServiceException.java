
// Package declaration for the external service-related classes
package gr.hua.dit.studyrooms.external;


/**
 * Custom unchecked exception to indicate errors when interacting with external services.
 */
public class ExternalServiceException extends RuntimeException {


    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message
     */
    public ExternalServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * @param message the detail message
     * @param cause the cause of the exception (can be retrieved later by the Throwable.getCause() method)
     */
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
