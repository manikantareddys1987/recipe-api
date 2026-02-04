package com.recipe.model.domain.request;

import com.recipe.config.ValidationConfig;
import com.recipe.model.entity.RecipeType;
import com.recipe.validator.EnumValidator;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CreateRecipeRequest {
    @NotBlank(message = "{recipe.name.not.blank}")
    @Size(max = ValidationConfig.MAX_LENGTH_NAME, message = "{recipe.name.size}")
    @Pattern(regexp = ValidationConfig.PATTERN_NAME, message = "{recipe.name.pattern}")
    @Schema(description = "The name of the recipe", example = "Pasta")
    private String name;

    @Schema(description = "The type of the recipe", example = "VEGETARIAN")
    @EnumValidator(enumClass = RecipeType.class, message = "{recipe.type.invalid}")
    private String type;

    @NotNull(message = "{number.of.servings.not.null}")
    @Positive(message = "{number.of.servings.positive}")
    @Schema(description = "The number of servings per recipe", example = "4")
    private int numberOfServings;

    @Schema(description = "The ids of the ingredients needed to make the recipe", example = "[1,2]")
    private List<Integer> ingredientIds;

    @NotBlank(message = "{instructions.not.blank}")
    @Size(max = ValidationConfig.MAX_LENGTH_DEFAULT, message = "{instructions.size}")
    @Pattern(regexp = ValidationConfig.PATTERN_FREE_TEXT, message = "{instructions.pattern}")
    @Schema(description = "The instructions to create the recipe", example = "Chop the tomato, stir and fry, boil and serve")
    private String instructions;

    public CreateRecipeRequest() {
    }

    public CreateRecipeRequest(String name, String type, int numberOfServings, List<Integer> ingredientIds, String instructions) {
        this.name = name;
        this.type = type;
        this.numberOfServings = numberOfServings;
        this.ingredientIds = ingredientIds;
        this.instructions = instructions;
    }

}