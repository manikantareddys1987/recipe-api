package com.recipe.unit.validator;

import com.recipe.model.domain.request.CreateIngredientRequest;
import org.junit.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class EnumValidatorConstraintTest {
    @SuppressWarnings("resource")
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void whenNotBlankName_thenNoConstraintViolations() {
        CreateIngredientRequest request = new CreateIngredientRequest("pasta");

        Set<ConstraintViolation<CreateIngredientRequest>> violations = validator.validate(request);

        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void whenBlankName_thenOneConstraintViolation() {
        CreateIngredientRequest request = new CreateIngredientRequest(null);

        Set<ConstraintViolation<CreateIngredientRequest>> violations = validator.validate(request);
        String collect = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        assertEquals("{ingredient.not.blank}", collect);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenEmptyName_thenOneConstraintViolation() {
        CreateIngredientRequest request = new CreateIngredientRequest(null);

        Set<ConstraintViolation<CreateIngredientRequest>> violations = validator.validate(request);
        String collect = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        assertEquals("{ingredient.not.blank}", collect);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenNameDoesNotFitPattern_thenOneConstraintViolation() {
        CreateIngredientRequest request = new CreateIngredientRequest("-.1!@$!#@");

        Set<ConstraintViolation<CreateIngredientRequest>> violations = validator.validate(request);
        String collect = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        assertEquals("{ingredient.pattern}", collect);
        assertThat(violations.size()).isEqualTo(1);
    }
}
