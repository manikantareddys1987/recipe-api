package com.recipe.model.domain.request;

import com.recipe.config.ValidationConfig;
import com.recipe.model.entity.RecipeType;
import com.recipe.validator.EnumValidator;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import java.util.List;

public class UpdateRecipeRequest extends BasicRequest{
    @NotBlank(message = "{recipe.name.not.blank}")
    @Size(max = ValidationConfig.MAX_LENGTH_NAME, message = "{recipe.name.size}")
    @Pattern(regexp = ValidationConfig.PATTERN_NAME, message = "{recipe.name.pattern}")
    @ApiModelProperty(notes = "The name of the ingredient", example = "Potato")
    private String name;

    @EnumValidator(enumClass = RecipeType.class, message = "{recipe.type.invalid}")
    @ApiModelProperty(notes = "The type of the recipe", example = "VEGETARIAN")
    private String type;

    @NotNull(message = "{number.of.servings.not.null}")
    @Positive(message = "{number.of.servings.positive}")
    @ApiModelProperty(notes = "The number of servings", example = "7")
    private int numberOfServings;

    @ApiModelProperty(notes = "The new ids of the ingredients needed for the update", example = "[3,4]")
    private List<Integer> ingredientIds;

    @NotBlank(message = "{instructions.not.blank}")
    @Size(max = ValidationConfig.MAX_LENGTH_DEFAULT, message = "{instructions.size}")
    @Pattern(regexp = ValidationConfig.PATTERN_FREE_TEXT, message = "{instructions.pattern}")
    @ApiModelProperty(notes = "The instructions to update the recipe", example = "Cut,fry,eat")

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

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getNumberOfServings() {
        return numberOfServings;
    }

    public List<Integer> getIngredientIds() {
        return ingredientIds;
    }

    public String getInstructions() {
        return instructions;
    }
}
