package com.recipe.model.search;

import com.recipe.model.entity.Recipe;
import com.recipe.config.DatabaseAttributes;
import com.recipe.model.search.filter.*;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

public class RecipeSpecification implements Specification<Recipe> {
    private final SearchCriteria criteria;

    // Modern Java: Using immutable List instead of mutable ArrayList
    private static final List<SearchFilter> searchFilters = List.of(
            new SearchFilterEqual(),
            new SearchFilterNotEqual(),
            new SearchFilterContains(),
            new SearchFilterDoesNotContain()
    );

    public RecipeSpecification(SearchCriteria criteria) {
        super();
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Optional<SearchOperation> operation = SearchOperation.getOperation(criteria.getOperation());
        String filterValue = criteria.getValue().toString().toLowerCase();
        String filterKey = criteria.getFilterKey();

        // Only join ingredients table when searching by ingredient field
        Join<Object, Object> subRoot = null;
        if ("ingredient".equalsIgnoreCase(filterKey)) {
            subRoot = root.join(DatabaseAttributes.JOINED_TABLE_NAME, JoinType.INNER);
            query.distinct(true);
        }

        final Join<Object, Object> finalSubRoot = subRoot;
        return operation.flatMap(searchOperation -> searchFilters
                .stream()
                .filter(searchFilter -> searchFilter.couldBeApplied(searchOperation))
                .findFirst()
                .map(searchFilter -> searchFilter.apply(cb, filterKey, filterValue, root, finalSubRoot))).orElse(null);
    }
}
