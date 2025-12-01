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
 * MVC-specific validation handler so HTML forms can redisplay with binding errors.
 */
@ControllerAdvice(basePackageClasses = {
        AuthController.class,
        ReservationController.class,
        StudySpaceController.class
})
public class WebExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelAndView handleValidationErrors(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        ModelAndView mav = new ModelAndView(resolveViewName(bindingResult.getTarget()));

        mav.addObject(bindingResult.getObjectName(), bindingResult.getTarget());
        mav.addObject(BindingResult.MODEL_KEY_PREFIX + bindingResult.getObjectName(), bindingResult);

        return mav;
    }

    private String resolveViewName(Object target) {
        if (target == null) {
            return "error";
        }
        if (target instanceof UserRegistrationDto) {
            return "register";
        }
        if (target instanceof ReservationFormDto) {
            return "reservation_form";
        }
        if (target instanceof StudySpaceDto) {
            return "space_form";
        }
        return "error";
    }
}

