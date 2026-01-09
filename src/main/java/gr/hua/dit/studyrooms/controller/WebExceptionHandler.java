
// Package declaration
package gr.hua.dit.studyrooms.controller;


import gr.hua.dit.studyrooms.dto.ReservationFormDto;
import gr.hua.dit.studyrooms.dto.UserRegistrationDto;
import gr.hua.dit.studyrooms.dto.StudySpaceDto;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;


/**
 * Handles validation errors for web (MVC) controllers so that HTML forms can redisplay with binding errors.
 * This class is annotated with @ControllerAdvice to apply globally to specified controllers.
 */
@ControllerAdvice(basePackageClasses = {
    AuthController.class,
    ReservationController.class,
    StudySpaceController.class
})
public class WebExceptionHandler {


    /**
     * Handles validation errors thrown by Spring when @Valid fails in a controller method.
     * This method catches MethodArgumentNotValidException and prepares a ModelAndView
     * with the appropriate form and error messages so the user can correct their input.
     *
     * @param ex the exception containing validation errors
     * @return ModelAndView for the form view with error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelAndView handleValidationErrors(MethodArgumentNotValidException ex) {
        // Extract the binding result containing validation errors
        BindingResult bindingResult = ex.getBindingResult();
        // Determine which view (form) to return based on the object being validated
        ModelAndView mav = new ModelAndView(resolveViewName(bindingResult.getTarget()));

        // Add the form object back to the model so the form can be repopulated
        mav.addObject(bindingResult.getObjectName(), bindingResult.getTarget());
        // Add the binding result (with errors) to the model so errors can be displayed
        mav.addObject(BindingResult.MODEL_KEY_PREFIX + bindingResult.getObjectName(), bindingResult);

        return mav;
    }


    /**
     * Determines which view (HTML template) to return based on the type of form object.
     * This ensures the user is sent back to the correct form with errors displayed.
     *
     * @param target the form object that failed validation
     * @return the name of the view to render
     */
    private String resolveViewName(Object target) {
        if (target == null) {
            // Fallback to a generic error page if target is missing
            return "error";
        }
        if (target instanceof UserRegistrationDto) {
            // Registration form
            return "register";
        }
        if (target instanceof ReservationFormDto) {
            // Reservation form
            return "reservation_form";
        }
        if (target instanceof StudySpaceDto) {
            // Study space form
            return "space_form";
        }
        // Default to error page for unknown types
        return "error";
    }
}

