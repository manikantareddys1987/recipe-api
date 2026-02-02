package com.recipe.exception;

import org.springframework.http.HttpStatus;

public interface ICustomException {
    HttpStatus getStatus();
}