package annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotSpaseValidator implements ConstraintValidator<NotSpase, String> {
    @Override
    public boolean isValid(String login, ConstraintValidatorContext constraintValidatorContext) {
        boolean valid = true;
        if (login != null) {
            valid = !login.contains(" ");
        }
        return  valid;
    }
}
