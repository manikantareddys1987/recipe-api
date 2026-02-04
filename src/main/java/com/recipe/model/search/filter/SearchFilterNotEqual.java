package com.recipe.model.search.filter;

import com.recipe.model.entity.Recipe;
import com.recipe.model.search.SearchOperation;
import com.recipe.config.DatabaseAttributes;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class SearchFilterNotEqual implements SearchFilter {

    @Override
    public boolean couldBeApplied(SearchOperation opt) {
        return opt == SearchOperation.NOT_EQUAL;
    }

    @Override
    public Predicate apply(CriteriaBuilder cb, String filterKey, String filterValue, Root<Recipe> root, Join<Object, Object> subRoot) {
        if (filterKey.equals(DatabaseAttributes.INGREDIENT_KEY)) {
            return cb.notEqual(cb.lower(subRoot.get(filterKey).as(String.class)), filterValue);
        }

        return cb.notEqual(cb.lower(root.get(filterKey).as(String.class)), filterValue);
    }
}
