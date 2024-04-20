package annotations;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class IsAfterDateValidation implements ConstraintValidator<IsAfterDate, LocalDate> {

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext constraintValidatorContext) {
        return  releaseDate.isAfter(LocalDate.of(1895, 12, 28));
    }

}
