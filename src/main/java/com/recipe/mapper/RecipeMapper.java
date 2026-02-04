package com.recipe.mapper;

import com.recipe.model.domain.request.CreateRecipeRequest;
import com.recipe.model.domain.request.UpdateRecipeRequest;
import com.recipe.model.domain.response.RecipeResponse;
import com.recipe.model.entity.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for Recipe entity conversions
 * Automatically maps fields with same names between DTOs and entities
 * Compile-time generation for zero-runtime overhead
 */
@Mapper(componentModel = "spring")
public interface RecipeMapper {

    RecipeMapper INSTANCE = Mappers.getMapper(RecipeMapper.class);

    /**
     * Maps CreateRecipeRequest DTO to Recipe entity
     * Field mapping is automatic for same-named fields
     *
     * @param request the create recipe request
     * @return mapped Recipe entity
     */
    Recipe createRequestToRecipe(CreateRecipeRequest request);

    /**
     * Maps Recipe entity to RecipeResponse DTO
     *
     * @param recipe the recipe entity
     * @return mapped recipe response
     */
    RecipeResponse recipeToResponse(Recipe recipe);

    /**
     * Updates existing Recipe entity from UpdateRecipeRequest
     * Only updates fields present in the request
     *
     * @param request the update recipe request
     * @param recipe the target recipe entity to update
     */
    void updateRequestToRecipe(UpdateRecipeRequest request, @MappingTarget Recipe recipe);
}

