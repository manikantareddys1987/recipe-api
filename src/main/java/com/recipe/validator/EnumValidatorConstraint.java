package com.recipe.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;

/**
 * Modern Java 21 validator using immutable Set instead of mutable ArrayList
 */
public class EnumValidatorConstraint implements ConstraintValidator<EnumValidator, String> {
    private Set<String> acceptedValues;

    @Override
    public void initialize(EnumValidator constraintAnnotation) {
        // Modern Java: Using Stream and Set.copyOf() for immutable collection
        acceptedValues = Arrays.stream(constraintAnnotation.enumClass().getEnumConstants())
                .map(e -> e.toString().toUpperCase())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return acceptedValues.contains(value.toUpperCase());
    }
}
