package com.recipe.integration;

import com.recipe.model.domain.request.CreateRecipeRequest;
import com.recipe.model.domain.request.RecipeSearchRequest;
import com.recipe.model.domain.request.SearchCriteriaRequest;
import com.recipe.model.domain.response.RecipeResponse;
import com.recipe.model.entity.Ingredient;
import com.recipe.model.entity.Recipe;
import com.recipe.repository.IngredientRepository;
import com.recipe.repository.RecipeRepository;
import com.recipe.unit.model.builder.IngredientTestDataBuilder;
import com.recipe.unit.model.builder.RecipeTestDataBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
public class RecipeIT extends AbstractIT {
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Before
    public void before() {
        recipeRepository.deleteAll();
    }

    @Test
    public void test_createRecipe_successfully() throws Exception {
        CreateRecipeRequest request = new CreateRecipeRequest("pasta",
                "VEGETARIAN", 5, null, "someInstruction");

        MvcResult result = performPost("/api/v1/recipe", request)
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = readByJsonPath(result, "$.id");
        Optional<Recipe> optionalRecipe = recipeRepository.findById(id);
        assertTrue(optionalRecipe.isPresent());
        assertEquals(optionalRecipe.get().getName(), request.getName());
    }

    @Test
    public void test_getRecipe_successfully() throws Exception {
        Recipe Recipe = RecipeTestDataBuilder.createRecipe();
        Recipe savedRecipe = recipeRepository.save(Recipe);

        performGet("/api/v1/recipe/" + savedRecipe.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRecipe.getId()))
                .andExpect(jsonPath("$.name").value(savedRecipe.getName()))
                .andExpect(jsonPath("$.instructions").value(savedRecipe.getInstructions()))
                .andExpect(jsonPath("$.numberOfServings").value(savedRecipe.getNumberOfServings()));
    }

    @Test
    public void test_getRecipe_notFound() throws Exception {

        performGet("/api/v1/recipe/1")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void test_listRecipe_successfully() throws Exception {
        Recipe recipe1 = new Recipe();
        recipe1.setId(5);
        recipe1.setName("name1");
        recipe1.setInstructions("Ins1");
        recipe1.setType("VEGETARIAN");
        recipe1.setNumberOfServings(2);

        Recipe recipe2 = new Recipe();
        recipe2.setId(6);
        recipe2.setName("name2");
        recipe2.setInstructions("Ins2");
        recipe2.setType("NON_VEGETARIAN");
        recipe2.setNumberOfServings(4);

        List<Recipe> storedRecipeList = new ArrayList<>();
        storedRecipeList.add(recipe1);
        storedRecipeList.add(recipe2);

        recipeRepository.saveAll(storedRecipeList);

        MvcResult result = performGet("/api/v1/recipe/page/0/size/10")
                .andExpect(status().isOk())
                .andReturn();

        List<RecipeResponse> RecipeList = getListFromMvcResult(result, RecipeResponse.class);

        assertEquals(storedRecipeList.size(), RecipeList.size());
        assertEquals(storedRecipeList.get(0).getName(), RecipeList.get(0).getName());
        assertEquals(storedRecipeList.get(1).getName(), RecipeList.get(1).getName());
    }

    @Test
    public void test_updateRecipe_successfully() throws Exception {
        Recipe testRecipe = new Recipe();
        testRecipe.setName("lasagna");
        testRecipe.setType("VEGETARIAN");
        testRecipe.setInstructions("chop the onion, potato");
        testRecipe.setNumberOfServings(2);

        Recipe savedRecipe = recipeRepository.save(testRecipe);

        savedRecipe.setName("meat-lasagna");
        savedRecipe.setInstructions("add meat add pasta");

        performPatch("/api/v1/recipe", savedRecipe)
                .andExpect(status().isOk());

        Optional<Recipe> updatedRecipe = recipeRepository.findById(savedRecipe.getId());

        assertTrue(updatedRecipe.isPresent());
        assertEquals(savedRecipe.getName(), updatedRecipe.get().getName());
        assertEquals(savedRecipe.getNumberOfServings(), updatedRecipe.get().getNumberOfServings());
        assertEquals(savedRecipe.getInstructions(), updatedRecipe.get().getInstructions());
    }

    @Test
    public void test_updateRecipe_idIsNull() throws Exception {
        Recipe testRecipe = new Recipe();
        testRecipe.setName("sarmale");
        testRecipe.setInstructions("take grape leaf, take meat, cook them");
        testRecipe.setNumberOfServings(3);
        testRecipe.setType("NON_VEGETARIAN");

        performPatch("/api/v1/recipe", testRecipe)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_updateRecipe_notFound() throws Exception {
        Recipe testRecipe = RecipeTestDataBuilder.createRecipe(1);

        performPatch("/api/v1/recipe", testRecipe)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void test_deleteRecipe_successfully() throws Exception {
        Recipe testRecipe = RecipeTestDataBuilder.createRecipe();
        Recipe savedRecipe = recipeRepository.save(testRecipe);

        performDelete("/api/v1/recipe", Pair.of("id", String.valueOf(savedRecipe.getId())))
                .andExpect(status().isOk());

        Optional<Recipe> deletedRecipe = recipeRepository.findById(savedRecipe.getId());

        assertTrue(deletedRecipe.isEmpty());
    }

    @Test
    public void test_deleteRecipe_notFound() throws Exception {
        performDelete("/api/v1/recipe", Pair.of("id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void test_SearchRecipeByCriteria_successfully() throws Exception {
        //create ingredient for recipe
        Ingredient ingredient = IngredientTestDataBuilder.createIngredientWithNameParam("Pepper");
        Ingredient savedIngredient = ingredientRepository.save(ingredient);

        //create the recipe
        CreateRecipeRequest createRecipeRequest = new CreateRecipeRequest("pasta",
                "VEGETARIAN", 5, List.of(savedIngredient.getId()), "someInstruction");

        MvcResult createdRecipe = performPost("/api/v1/recipe", createRecipeRequest)
                .andExpect(status().isCreated())
                .andReturn();

        //prepare the search criteria and by newly created id
        Integer id = readByJsonPath(createdRecipe, "$.id");

        RecipeSearchRequest request = new RecipeSearchRequest();
        List<SearchCriteriaRequest> searchCriteriaList = new ArrayList<>();
        SearchCriteriaRequest searchCriteria = new SearchCriteriaRequest("name",
                "pasta",
                "cn");

        searchCriteriaList.add(searchCriteria);

        request.setDataOption("ALL");
        request.setSearchCriteriaRequests(searchCriteriaList);

        //call search endpoint by previously created criteria
        MvcResult result = performPost("/api/v1/recipe/search", request)
                .andExpect(status().isOk())
                .andReturn();

        Optional<Recipe> optionalRecipe = recipeRepository.findById(id);


        List<RecipeResponse> listRecipeList = getListFromMvcResult(result, RecipeResponse.class);
        assertFalse(listRecipeList.isEmpty());
        Assert.assertTrue(optionalRecipe.isPresent());
        Assert.assertEquals(listRecipeList.getFirst().getName(), optionalRecipe.get().getName());
        Assert.assertEquals(listRecipeList.getFirst().getInstructions(), optionalRecipe.get().getInstructions());
        Assert.assertEquals(listRecipeList.getFirst().getNumberOfServings(), optionalRecipe.get().getNumberOfServings());
    }

    @Test
    public void test_SearchRecipeByCriteria_fails() throws Exception {
        RecipeSearchRequest request = new RecipeSearchRequest();
        // Send empty search request which should fail validation

        performPost("/api/v1/recipe/search", request)
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").exists())
                .andReturn();
    }

}
