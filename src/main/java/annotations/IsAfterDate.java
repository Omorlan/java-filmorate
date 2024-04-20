package annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {IsAfterDateValidation.class})
public @interface IsAfterDate {
    String message() default "дожна быть раньше 28.12.1985";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
