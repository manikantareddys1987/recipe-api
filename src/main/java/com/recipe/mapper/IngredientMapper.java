package com.recipe.mapper;

import com.recipe.model.domain.request.CreateIngredientRequest;
import com.recipe.model.domain.response.IngredientResponse;
import com.recipe.model.entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for Ingredient entity conversions
 */
@Mapper(componentModel = "spring")
public interface IngredientMapper {

    IngredientMapper INSTANCE = Mappers.getMapper(IngredientMapper.class);

    /**
     * Maps CreateIngredientRequest to Ingredient entity
     * Explicitly maps name field to ingredient column
     */
    @Mapping(source = "name", target = "ingredient")
    Ingredient createRequestToIngredient(CreateIngredientRequest request);

    /**
     * Maps Ingredient entity to IngredientResponse DTO
     */
    IngredientResponse ingredientToResponse(Ingredient ingredient);
}

