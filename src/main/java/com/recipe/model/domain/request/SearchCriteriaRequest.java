package com.recipe.model.domain.request;

import com.recipe.model.domain.request.input.FilterKeyReqInput;
import com.recipe.model.domain.request.input.SearchOperationReqInput;
import com.recipe.validator.EnumValidator;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Valid
public class SearchCriteriaRequest {

    @Schema(description = "The name of the column to search on (available: name, numberOfServings, type, instructions, ingredientName)", example = "name")
    @EnumValidator(enumClass = FilterKeyReqInput.class, message = "{filter.key.invalid}")
    private String filterKey;


    @Schema(description = "The phrase to search for", example = "Pasta")
    private Object value;

    @Schema(description = "The operation type (cn - contains, nc - doesn't contain, eq - equals, ne - not equals)", example = "cn")
    @EnumValidator(enumClass = SearchOperationReqInput.class, message = "{search.operation.invalid}")
    private String operation;

    @Schema(hidden = true)
    private String dataOption;

    public SearchCriteriaRequest() {
    }

    public SearchCriteriaRequest(String filterKey, Object value, String operation) {
        this.filterKey = filterKey;
        this.value = value;
        this.operation = operation;
    }

}