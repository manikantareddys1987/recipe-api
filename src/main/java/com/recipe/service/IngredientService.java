package com.recipe.service;

import com.recipe.model.domain.request.CreateIngredientRequest;
import com.recipe.config.MessageProvider;
import com.recipe.exception.NotFoundException;
import com.recipe.mapper.IngredientMapper;
import com.recipe.model.entity.Ingredient;
import com.recipe.repository.IngredientRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final MessageProvider messageProvider;
    private final IngredientMapper ingredientMapper;

    public IngredientService(IngredientRepository ingredientRepository,
                             MessageProvider messageProvider,
                             IngredientMapper ingredientMapper) {
        this.ingredientRepository = ingredientRepository;
        this.messageProvider = messageProvider;
        this.ingredientMapper = ingredientMapper;
    }

    public Integer create(CreateIngredientRequest request) {
        // Automatically map DTO to entity
        Ingredient ingredient = ingredientMapper.createRequestToIngredient(request);
        Ingredient createdIngredient = ingredientRepository.save(ingredient);
        return createdIngredient.getId();
    }


    public Set<Ingredient> getIngredientsByIds(List<Integer> ingredientIds) {
        return Set.copyOf(ingredientIds.stream()
                .map(this::findById)
                .toList());
    }

    public Ingredient findById(int id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageProvider.getMessage("ingredient.not.found")));
    }

    public List<Ingredient> getIngredientsByPageAndSize(int page, int size) {
        Pageable pageRequest
                = PageRequest.of(page, size);
        return ingredientRepository.findAll(pageRequest).getContent();
    }

    public void deleteIngredient(int id) {
        if (!ingredientRepository.existsById(id)) {
            throw new NotFoundException(messageProvider.getMessage("ingredient.not.found"));
        }
        ingredientRepository.deleteById(id);
    }
}
