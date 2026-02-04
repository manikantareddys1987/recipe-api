package com.recipe.model.domain.request;

import com.recipe.config.ValidationConfig;
import com.recipe.model.entity.RecipeType;
import com.recipe.validator.EnumValidator;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateRecipeRequest extends BasicRequest{
    @NotBlank(message = "{recipe.name.not.blank}")
    @Size(max = ValidationConfig.MAX_LENGTH_NAME, message = "{recipe.name.size}")
    @Pattern(regexp = ValidationConfig.PATTERN_NAME, message = "{recipe.name.pattern}")
    @Schema(description = "The name of the ingredient", example = "Potato")
    private String name;

    @EnumValidator(enumClass = RecipeType.class, message = "{recipe.type.invalid}")
    @Schema(description = "The type of the recipe", example = "VEGETARIAN")
    private String type;

    @NotNull(message = "{number.of.servings.not.null}")
    @Positive(message = "{number.of.servings.positive}")
    @Schema(description = "The number of servings", example = "7")
    private int numberOfServings;

    @Schema(description = "The new ids of the ingredients needed for the update", example = "[3,4]")
    private List<Integer> ingredientIds;

    @NotBlank(message = "{instructions.not.blank}")
    @Size(max = ValidationConfig.MAX_LENGTH_DEFAULT, message = "{instructions.size}")
    @Pattern(regexp = ValidationConfig.PATTERN_FREE_TEXT, message = "{instructions.pattern}")
    @Schema(description = "The instructions to update the recipe", example = "Cut,fry,eat")

    private String instructions;

    public UpdateRecipeRequest() {
    }

    public UpdateRecipeRequest(Integer id, String name, String type, int numberOfServings, List<Integer> ingredientIds, String instructions) {
        super(id);
        this.name = name;
        this.type = type;
        this.numberOfServings = numberOfServings;
        this.ingredientIds = ingredientIds;
        this.instructions = instructions;
    }

}
