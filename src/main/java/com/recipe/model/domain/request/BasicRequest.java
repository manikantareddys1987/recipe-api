package com.recipe.model.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class BasicRequest {

    @NotNull(message = "{id.not.null}")
    @Positive(message = "{id.positive}")
    @Schema(description = "Id of the attribute", example = "1")
    private Integer id;

    public BasicRequest() {
    }

    public BasicRequest(Integer id) {
        this.id = id;
    }
}
