package org.kds.reactive.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class AtValidator implements ConstraintValidator<MustContainAt, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return Objects.nonNull(value) && value.contains("@");
    }

    @Override
    public void initialize(MustContainAt constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
