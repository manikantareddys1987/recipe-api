package com.recipe.integration;

import com.recipe.model.domain.request.CreateIngredientRequest;
import com.recipe.model.domain.response.IngredientResponse;
import com.recipe.model.entity.Ingredient;
import com.recipe.repository.IngredientRepository;
import com.recipe.unit.model.builder.IngredientTestDataBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IngredientIT extends AbstractIT {
    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    protected MockMvc mockMvc;

    @Before
    public void before() {
        ingredientRepository.deleteAll();
    }


    @Test
    public void test_createIngredient_successfully() throws Exception {
        CreateIngredientRequest request = IngredientTestDataBuilder.createIngredientRequest();

        MvcResult result = performPost("/ingredient", request)
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = readByJsonPath(result, "$.id");

        Optional<Ingredient> ingredient = ingredientRepository.findById(id);

        assertTrue(ingredient.isPresent());
        assertEquals(ingredient.get().getIngredient(), request.getName());
    }

    @Test
    public void test_listIngredient_successfully() throws Exception {
        Ingredient ingredient = IngredientTestDataBuilder.createIngredient();
        Ingredient savedIngredient = ingredientRepository.save(ingredient);

        performGet("/ingredient/" + savedIngredient.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedIngredient.getId()))
                .andExpect(jsonPath("$.name").value(ingredient.getIngredient()))
                .andExpect(jsonPath("$.createdAt").value(ingredient.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .andExpect(jsonPath("$.updatedAt").value(ingredient.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
    }

    @Test
    public void test_listIngredient_notFound() throws Exception {
        performGet("/ingredient/1")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void test_listIngredients_successfully() throws Exception {


        List<Ingredient> ingredientList = IngredientTestDataBuilder.createIngredientList();
        ingredientRepository.saveAll(ingredientList);

        MvcResult result = performGet("/ingredient/page/0/size/10")
                .andExpect(status().isOk())
                .andReturn();

        List<IngredientResponse> responses = getListFromMvcResult(result, IngredientResponse.class);

        assertEquals(ingredientList.size(), responses.size());
    }

    @Test
    public void test_deleteIngredients_successfully() throws Exception {
        Ingredient ingredient = IngredientTestDataBuilder.createIngredient();
        Ingredient savedIngredient = ingredientRepository.save(ingredient);

        performDelete("/ingredient?id=" + savedIngredient.getId())
                .andExpect(status().isOk());

        Optional<Ingredient> deletedIngredient = ingredientRepository.findById(savedIngredient.getId());
        assertTrue(deletedIngredient.isEmpty());
    }

    @Test
    public void test_findIngredientById_successfully() throws Exception {
        Ingredient ingredient = IngredientTestDataBuilder.createIngredient();
        Ingredient savedIngredient = ingredientRepository.save(ingredient);

        MvcResult result = performGet("/ingredient/" + savedIngredient.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedIngredient.getId()))
                .andExpect(jsonPath("$.name").value(savedIngredient.getIngredient()))
                .andReturn();

        IngredientResponse ingredientResponse = getFromMvcResult(result, IngredientResponse.class);
        assertEquals(savedIngredient.getIngredient(), ingredientResponse.getName());
    }

    @Test
    public void test_findIngredientById_fails() throws Exception {

        performGet("/ingredient/" + 1)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void test_deleteIngredient_notFound() throws Exception {

        performDelete("/ingredient?id=11")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
