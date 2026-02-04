package com.recipe.model.domain.request;

import com.recipe.model.domain.request.input.DataOptionReqInput;
import com.recipe.validator.EnumValidator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RecipeSearchRequest {
    @JsonProperty("criteria")
    @Schema(description = "Search criteria you want to search recipe with")
    @Valid
    private List<SearchCriteriaRequest> searchCriteriaRequests;

    @Schema(description = "If you want all or just one criteria is enough for filter to work", example = "all")
    @EnumValidator(enumClass = DataOptionReqInput.class, message = "{data.option.invalid}")
    private String dataOption;

    public RecipeSearchRequest() {
    }

    public RecipeSearchRequest(List<SearchCriteriaRequest> searchCriteriaRequests, String dataOption) {
        this.searchCriteriaRequests = searchCriteriaRequests;
        this.dataOption = dataOption;
    }

}
