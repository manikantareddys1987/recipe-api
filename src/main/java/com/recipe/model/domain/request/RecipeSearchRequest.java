package com.recipe.model.domain.request;

import com.recipe.model.domain.request.input.DataOptionReqInput;
import com.recipe.validator.EnumValidator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.List;

public class RecipeSearchRequest {
    @JsonProperty("criteria")
    @ApiModelProperty(notes = "Search criteria you want to search recipe with")
    @Valid
    private List<SearchCriteriaRequest> searchCriteriaRequests;

    @ApiModelProperty(notes = "If you want all or just one criteria is enough for filter to work", example = "all")
    @EnumValidator(enumClass = DataOptionReqInput.class, message = "{data.option.invalid}")
    private String dataOption;

    public RecipeSearchRequest() {
    }

    public RecipeSearchRequest(List<SearchCriteriaRequest> searchCriteriaRequests, String dataOption) {
        this.searchCriteriaRequests = searchCriteriaRequests;
        this.dataOption = dataOption;
    }

    public List<SearchCriteriaRequest> getSearchCriteriaRequests() {
        return searchCriteriaRequests;
    }

    public String getDataOption() {
        return dataOption;
    }

    public void setDataOption(String dataOption) {
        this.dataOption = dataOption;
    }

    public void setSearchCriteriaRequests(List<SearchCriteriaRequest> searchCriteriaRequests) {
        this.searchCriteriaRequests = searchCriteriaRequests;
    }
}
