package com.recipe.model.search.filter;

import com.recipe.model.entity.Recipe;
import com.recipe.model.search.SearchOperation;
import com.recipe.config.DatabaseAttributes;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class SearchFilterEqual implements SearchFilter {

    @Override
    public boolean couldBeApplied(SearchOperation opt) {
        return opt == SearchOperation.EQUAL;
    }

    @Override
    public Predicate apply(CriteriaBuilder cb, String filterKey, String filterValue, Root<Recipe> root, Join<Object, Object> subRoot) {
        if (filterKey.equals(DatabaseAttributes.INGREDIENT_KEY)) {
            return cb.equal(cb.lower(subRoot.get(filterKey).as(String.class)), filterValue);
        }

        return cb.equal(cb.lower(root.get(filterKey).as(String.class)), filterValue);
    }
}
