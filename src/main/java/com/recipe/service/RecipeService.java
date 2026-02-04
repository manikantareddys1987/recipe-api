package com.recipe.service;

import com.recipe.model.domain.request.CreateRecipeRequest;
import com.recipe.model.domain.request.RecipeSearchRequest;
import com.recipe.model.domain.request.SearchCriteriaRequest;
import com.recipe.model.domain.request.UpdateRecipeRequest;
import com.recipe.model.domain.response.RecipeResponse;
import com.recipe.config.MessageProvider;
import com.recipe.config.MetricsConfig;
import com.recipe.exception.NotFoundException;
import com.recipe.mapper.RecipeMapper;
import com.recipe.model.entity.Ingredient;
import com.recipe.model.entity.Recipe;
import com.recipe.repository.RecipeRepository;
import com.recipe.model.search.RecipeSpecificationBuilder;
import com.recipe.model.search.SearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class RecipeService {
    private static final Logger logger = LoggerFactory.getLogger(RecipeService.class);

    private final RecipeRepository recipeRepository;
    private final IngredientService ingredientService;
    private final MessageProvider messageProvider;
    private final MetricsConfig metricsConfig;
    private final RecipeMapper recipeMapper;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository,
                         IngredientService ingredientService,
                         MessageProvider messageProvider,
                         MetricsConfig metricsConfig,
                         RecipeMapper recipeMapper) {
        this.recipeRepository = recipeRepository;
        this.ingredientService = ingredientService;
        this.messageProvider = messageProvider;
        this.metricsConfig = metricsConfig;
        this.recipeMapper = recipeMapper;
    }

    public Integer createRecipe(CreateRecipeRequest createRecipeRequest) {
        logger.info("Creating recipe: {}", createRecipeRequest.getName());

        // Automatically map DTO to entity
        Recipe recipe = recipeMapper.createRequestToRecipe(createRecipeRequest);

        // Set ingredients if provided
        Set<Ingredient> ingredients = Optional.ofNullable(createRecipeRequest.getIngredientIds())
                .map(ingredientService::getIngredientsByIds)
                .orElse(null);
        recipe.setRecipeIngredients(ingredients);

        Recipe createdRecipe = recipeRepository.save(recipe);
        metricsConfig.getRecipeCreatedCounter().increment();
        logger.info("Recipe created with ID: {}", createdRecipe.getId());

        return createdRecipe.getId();
    }

    public List<Recipe> getRecipeList(int page, int size) {
        logger.debug("Fetching recipe list - page: {}, size: {}", page, size);
        try {
            return metricsConfig.getRecipeRetrievalTimer().recordCallable(() -> {
                Pageable pageRequest = PageRequest.of(page, size);
                return recipeRepository.findAll(pageRequest).getContent();
            });
        } catch (IllegalArgumentException e) {
            logger.error("Invalid pagination parameters - page: {}, size: {}", page, size, e);
            throw new IllegalArgumentException("Invalid page or size parameter", e);
        } catch (Exception e) {
            logger.error("Error fetching recipe list - page: {}, size: {}", page, size, e);
            throw new RuntimeException("Failed to retrieve recipe list", e);
        }
    }

    public Recipe getRecipeById(int id) {
        logger.debug("Fetching recipe by ID: {}", id);
        try {
            return metricsConfig.getRecipeRetrievalTimer().recordCallable(() ->
                recipeRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException(messageProvider.getMessage("recipe.not.found")))
            );
        } catch (NotFoundException e) {
            logger.warn("Recipe not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching recipe by ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve recipe", e);
        }
    }

    public void updateRecipe(UpdateRecipeRequest updateRecipeRequest) {
        logger.info("Updating recipe ID: {}", updateRecipeRequest.getId());
        Recipe recipe = recipeRepository.findById(updateRecipeRequest.getId())
                .orElseThrow(() -> new NotFoundException(messageProvider.getMessage("recipe.not.found")));

        // Automatically map DTO fields to entity
        recipeMapper.updateRequestToRecipe(updateRecipeRequest, recipe);

        // Set ingredients if provided
        Set<Ingredient> ingredients = Optional.ofNullable(updateRecipeRequest.getIngredientIds())
                .map(ingredientService::getIngredientsByIds)
                .orElse(null);
        if (Optional.ofNullable(ingredients).isPresent()) {
            recipe.setRecipeIngredients(ingredients);
        }

        recipeRepository.save(recipe);
        metricsConfig.getRecipeUpdatedCounter().increment();
        logger.info("Recipe updated: {}", updateRecipeRequest.getId());
    }

    public void deleteRecipe(int id) {
        logger.info("Deleting recipe ID: {}", id);
        if (!recipeRepository.existsById(id)) {
            throw new NotFoundException(messageProvider.getMessage("recipe.not.found"));
        }

        recipeRepository.deleteById(id);
        metricsConfig.getRecipeDeletedCounter().increment();
        logger.info("Recipe deleted: {}", id);
    }

    public List<RecipeResponse> findBySearchCriteria(RecipeSearchRequest recipeSearchRequest, int page, int size) {
        logger.info("Searching recipes with criteria - page: {}, size: {}", page, size);
        try {
            return metricsConfig.getRecipeSearchTimer().recordCallable(() -> {
                List<SearchCriteria> searchCriterionRequests = new ArrayList<>();
                RecipeSpecificationBuilder builder = new RecipeSpecificationBuilder(searchCriterionRequests);
                Pageable pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());

                Specification<Recipe> recipeSpecification = createRecipeSpecification(recipeSearchRequest, builder);
                Page<Recipe> filteredRecipes = recipeRepository.findAll(recipeSpecification, pageRequest);

                metricsConfig.getRecipeSearchCounter().increment();
                logger.info("Search completed - found {} recipes", filteredRecipes.getTotalElements());

                return filteredRecipes.toList().stream()
                        .map(RecipeResponse::new)
                        .toList();
            });
        } catch (NotFoundException e) {
            logger.warn("Search criteria not valid: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid search parameters - page: {}, size: {}", page, size, e);
            throw new IllegalArgumentException("Invalid pagination or search parameters", e);
        } catch (Exception e) {
            logger.error("Error searching recipes with criteria - page: {}, size: {}", page, size, e);
            throw new RuntimeException("Failed to search recipes", e);
        }
    }

    private Specification<Recipe> createRecipeSpecification(RecipeSearchRequest recipeSearchRequest,
                                                            RecipeSpecificationBuilder builder) {
        List<SearchCriteriaRequest> searchCriteriaRequests = recipeSearchRequest.getSearchCriteriaRequests();

        if (searchCriteriaRequests != null && !searchCriteriaRequests.isEmpty()) {
            recipeSearchRequest.getSearchCriteriaRequests().stream()
                    .map(SearchCriteria::new)
                    .peek(criteria -> criteria.setDataOption(recipeSearchRequest.getDataOption()))
                    .forEach(builder::with);
        }

        return builder
                .build()
                .orElseThrow(() -> new NotFoundException(messageProvider.getMessage("criteria.not.found")));
    }
}
