package com.recipe.controller;

import com.recipe.model.domain.request.CreateRecipeRequest;
import com.recipe.model.domain.request.RecipeSearchRequest;
import com.recipe.model.domain.request.UpdateRecipeRequest;
import com.recipe.model.domain.response.CreateEntityResponse;
import com.recipe.model.domain.response.RecipeResponse;
import com.recipe.model.entity.Recipe;
import com.recipe.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Tag(name = "RecipeController", description = "Create, update, delete, list recipes")
@RestController
@RequestMapping(value = "/api/v1/recipe")
public class RecipeController {
    private final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    private final RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @Operation(summary = "List all recipes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful request"),
    })
    @RequestMapping(method = RequestMethod.GET, path = "/page/{page}/size/{size}")
    public List<RecipeResponse> getRecipeList(@PathVariable(name = "page") int page,
                                              @PathVariable(name = "size") int size) {
        logger.info("Getting the recipes");
        List<Recipe> list = recipeService.getRecipeList(page, size);

        return list.stream()
                .map(RecipeResponse::new)
                .toList();
    }

    @Operation(summary = "List one recipe by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful request"),
            @ApiResponse(responseCode = "404", description = "Recipe not found by the given ID")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public RecipeResponse getRecipe(@Parameter(description = "Recipe ID") @PathVariable(name = "id") Integer id) {
        logger.info("Getting the recipe by its id. Id: {}", id);
        Recipe recipe = recipeService.getRecipeById(id);
        return new RecipeResponse(recipe);
    }

    @Operation(summary = "Create a recipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recipe created"),
            @ApiResponse(responseCode = "400", description = "Bad input")
    })
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CreateEntityResponse createRecipe(
            @Parameter(description = "Properties of the recipe") @Valid @RequestBody CreateRecipeRequest request) {
        logger.info("Creating the recipe with properties");
        Integer id = recipeService.createRecipe(request);
        return new CreateEntityResponse(id);
    }

    @Operation(summary = "Update the recipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipe updated"),
            @ApiResponse(responseCode = "400", description = "Bad input")
    })
    @RequestMapping(method = RequestMethod.PATCH)
    public void updateRecipe(
            @Parameter(description = "Properties of the recipe") @Valid @RequestBody UpdateRecipeRequest updateRecipeRequest) {
        logger.info("Updating the recipe by given properties");
        recipeService.updateRecipe(updateRecipeRequest);
    }

    @Operation(summary = "Delete the recipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Recipe not found by the given ID")
    })
    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteRecipe(
            @Parameter(description = "Recipe ID") @NotNull(message = "{id.not.null}") @RequestParam(name = "id") Integer id) {
        logger.info("Deleting the recipe by its id. Id: {}", id);
        recipeService.deleteRecipe(id);
    }

    @Operation(summary = "Search recipes by given parameters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful request"),
            @ApiResponse(responseCode = "404", description = "Different error messages related to criteria and recipe")

    })
    @RequestMapping(method = RequestMethod.POST, path = "/search")
    public List<RecipeResponse> searchRecipe(@RequestParam(name = "page", defaultValue = "0") int page,
                                             @RequestParam(name = "size", defaultValue = "10") int size,
                                             @Parameter(description = "Properties of the search")
                                             @RequestBody @Valid RecipeSearchRequest recipeSearchRequest) {
        logger.info("Searching the recipe by given criteria");
        return recipeService.findBySearchCriteria(recipeSearchRequest, page, size);
    }
}
