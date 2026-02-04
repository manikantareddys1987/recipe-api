package com.recipe.controller;

import com.recipe.model.domain.request.CreateIngredientRequest;
import com.recipe.model.domain.response.CreateEntityResponse;
import com.recipe.model.domain.response.IngredientResponse;
import com.recipe.model.entity.Ingredient;
import com.recipe.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Tag(name = "IngredientController", description = "Create, update, delete, list ingredients")
@RestController
@RequestMapping(value = "/api/v1/ingredient")
public class IngredientController {

    private final Logger logger = LoggerFactory.getLogger(IngredientController.class);

    private final IngredientService ingredientService;

    @Autowired
    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @Operation(summary = "List all ingredients")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful request"),
    })
    @RequestMapping(method = RequestMethod.GET, path = "/page/{page}/size/{size}")
    public List<IngredientResponse> getIngredientList(@PathVariable(name = "page") int page,
                                                      @PathVariable(name = "size") int size) {
        logger.info("Getting the ingredients");
        List<Ingredient> list = ingredientService.getIngredientsByPageAndSize(page, size);

        return list.stream()
                .map(IngredientResponse::new)
                .toList();
    }

    @Operation(summary = "List one ingredient by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful request"),
            @ApiResponse(responseCode = "404", description = "Ingredient not found by the given ID")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public IngredientResponse getIngredient(@Parameter(description = "Ingredient ID") @PathVariable(name = "id") Integer id) {
        logger.info("Getting the ingredient by its id. Id: {}", id);
        Ingredient ingredient = ingredientService.findById(id);
        return new IngredientResponse(ingredient);
    }

    @Operation(summary = "Create an ingredient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ingredient created"),
            @ApiResponse(responseCode = "400", description = "Bad input")
    })
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CreateEntityResponse createIngredient(
            @Parameter(description = "Properties of the Ingredient") @Valid @RequestBody CreateIngredientRequest request) {
        logger.info("Creating the ingredient with properties");
        Integer id = ingredientService.create(request);
        return new CreateEntityResponse(id);
    }

    @Operation(summary = "Delete the ingredient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Ingredient not found by the given ID")
    })
    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteIngredient(@Parameter(description = "ingredient ID") @NotNull(message = "{id.not.null}") @RequestParam(name = "id") Integer id) {
        logger.info("Deleting the ingredient by its id. Id: {}", id);
        ingredientService.deleteIngredient(id);
    }
}
