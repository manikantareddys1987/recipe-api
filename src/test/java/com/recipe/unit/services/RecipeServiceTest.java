package com.recipe.unit.services;

import com.recipe.model.domain.request.CreateRecipeRequest;
import com.recipe.model.domain.request.RecipeSearchRequest;
import com.recipe.model.domain.request.UpdateRecipeRequest;
import com.recipe.config.MessageProvider;
import com.recipe.config.MetricsConfig;
import com.recipe.exception.NotFoundException;
import com.recipe.mapper.RecipeMapper;
import com.recipe.model.entity.Recipe;
import com.recipe.repository.RecipeRepository;
import com.recipe.service.IngredientService;
import com.recipe.service.RecipeService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class RecipeServiceTest {
    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientService ingredientService;

    @Mock
    private MessageProvider messageProvider;

    @Mock
    private MetricsConfig metricsConfig;

    @Mock
    private RecipeMapper recipeMapper;

    @InjectMocks
    private RecipeService recipeService;

    @Before
    public void setUp() throws Exception {
        // Setup all Timer mocks
        Timer searchTimer = mock(Timer.class);
        Timer retrievalTimer = mock(Timer.class);

        // Setup all Counter mocks
        Counter searchCounter = mock(Counter.class);
        Counter createdCounter = mock(Counter.class);
        Counter updatedCounter = mock(Counter.class);
        Counter deletedCounter = mock(Counter.class);

        // Stub Timer methods
        lenient().when(metricsConfig.getRecipeSearchTimer()).thenReturn(searchTimer);
        lenient().when(metricsConfig.getRecipeRetrievalTimer()).thenReturn(retrievalTimer);

        // Stub Counter methods - lenient for counters not used in all tests
        lenient().when(metricsConfig.getRecipeSearchCounter()).thenReturn(searchCounter);
        lenient().when(metricsConfig.getRecipeCreatedCounter()).thenReturn(createdCounter);
        lenient().when(metricsConfig.getRecipeUpdatedCounter()).thenReturn(updatedCounter);
        lenient().when(metricsConfig.getRecipeDeletedCounter()).thenReturn(deletedCounter);

        // Configure Timer behavior to execute the callable and handle exceptions
        doAnswer(invocation -> {
            Callable<?> callable = invocation.getArgument(0);
            try {
                return callable.call();
            } catch (NotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).when(searchTimer).recordCallable(any(Callable.class));

        lenient().doAnswer(invocation -> {
            Callable<?> callable = invocation.getArgument(0);
            try {
                return callable.call();
            } catch (NotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).when(retrievalTimer).recordCallable(any(Callable.class));
    }

    @Test
    public void test_createRecipe_successfully() {
        CreateRecipeRequest request = new CreateRecipeRequest("pasta", "OTHER", 4, null, "instructions");

        Recipe mappedRecipe = new Recipe();
        Recipe response = new Recipe();
        response.setName("Name");
        response.setInstructions("instructions");
        response.setNumberOfServings(4);
        response.setId(1);

        lenient().when(recipeMapper.createRequestToRecipe(any(CreateRecipeRequest.class))).thenReturn(mappedRecipe);
        lenient().when(ingredientService.getIngredientsByIds(any())).thenReturn(null);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(response);

        Integer id = recipeService.createRecipe(request);

        assertThat(id).isEqualTo(response.getId());
    }

    @Test
    public void test_updateRecipe_successfully() {
        Recipe response = new Recipe();
        response.setName("Name");
        response.setType("OTHER");
        response.setNumberOfServings(4);
        response.setId(5);

        UpdateRecipeRequest request = new UpdateRecipeRequest(1, "pasta", "OTHER", 4, null, "instructions");

        when(recipeRepository.findById(anyInt())).thenReturn(Optional.of(response));
        lenient().doNothing().when(recipeMapper).updateRequestToRecipe(any(UpdateRecipeRequest.class), any(Recipe.class));
        lenient().when(ingredientService.getIngredientsByIds(any())).thenReturn(null);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(response);

        recipeService.updateRecipe(request);
    }

    @Test
    public void test_updateRecipe_notFound() {
        UpdateRecipeRequest request = new UpdateRecipeRequest(1, "pasta", "OTHER", 4, null, "instructions");

        when(recipeRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(messageProvider.getMessage("recipe.not.found")).thenReturn("Recipe not found");

        assertThatThrownBy(() -> recipeService.updateRecipe(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void test_deleteRecipe_successfully() {
        when(recipeRepository.existsById(anyInt())).thenReturn(true);
        doNothing().when(recipeRepository).deleteById(anyInt());

        recipeService.deleteRecipe(1);
    }

    @Test
    public void test_deleteRecipe_notFound() {
        when(recipeRepository.existsById(anyInt())).thenReturn(false);
        when(messageProvider.getMessage("recipe.not.found")).thenReturn("Recipe not found");

        assertThatThrownBy(() -> recipeService.deleteRecipe(1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void test_findBySearchCriteria_notFound() {
        RecipeSearchRequest recipeSearchRequest = mock(RecipeSearchRequest.class);
        when(recipeSearchRequest.getSearchCriteriaRequests()).thenReturn(null);
        when(messageProvider.getMessage("criteria.not.found")).thenReturn("Criteria not found");

        assertThatThrownBy(() -> recipeService.findBySearchCriteria(recipeSearchRequest, 0, 10))
                .isInstanceOf(NotFoundException.class);
    }

}