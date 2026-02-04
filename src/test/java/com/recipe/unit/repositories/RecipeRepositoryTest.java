package com.recipe.unit.repositories;

import com.recipe.model.entity.Recipe;
import com.recipe.model.entity.RecipeType;
import com.recipe.repository.RecipeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class RecipeRepositoryTest {
    @Autowired
    private RecipeRepository recipeRepository;

    @Test
    public void test_whenTryToSaveIngredientSuccess() {
        Recipe entity = new Recipe();
        entity.setType(RecipeType.VEGETARIAN.name());
        entity.setInstructions("some instructions");
        entity.setName("pasta");
        entity.setNumberOfServings(4);
        Recipe savedRecipe = recipeRepository.save(entity);
        assertNotNull(savedRecipe);

        assertEquals(RecipeType.VEGETARIAN.name(), savedRecipe.getType());
        assertNotNull(savedRecipe.getId());
    }

    @Test
    public void test_whenTryGetTokenListSuccess() {
        Recipe entity1 = new Recipe();
        entity1.setType("Other");
        entity1.setName("lasagna");

        Recipe entity2 = new Recipe();
        entity2.setType("Other");
        entity2.setName("pizza");


        Recipe firstSavedEntity = recipeRepository.save(entity1);
        Recipe secondSavedEntity = recipeRepository.save(entity2);
        assertNotNull(firstSavedEntity);
        assertNotNull(secondSavedEntity);

        assertFalse(recipeRepository.findAll().isEmpty());
        assertEquals(2, recipeRepository.findAll().size());
    }
}