package com.recipe.exception;

import com.recipe.config.MessageProvider;
import com.recipe.model.domain.response.GenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Handles all exceptions thrown by controllers and services.
 * Uses @ControllerAdvice to intercept exceptions across all controllers.
 */
@ControllerAdvice
public class ExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);
    private final MessageProvider messageProvider;

    @Autowired
    public ExceptionController(MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    // ==================== Custom Application Exceptions ====================

    /**
     * Handles NotFoundException - when a resource is not found (404)
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleNotFoundException(NotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        HttpStatus status = ex.getStatus() == null ? HttpStatus.NOT_FOUND : ex.getStatus();
        return buildResponse(ex.getMessage(), status);
    }

    // ==================== Validation Exceptions ====================

    /**
     * Handles validation errors from @Valid annotation on request body (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        String errorMessage = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return buildResponse(errorMessage, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles constraint violations from @Validated annotation (400)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleConstraintViolation(ConstraintViolationException ex) {
        logger.warn("Constraint violation: {}", ex.getMessage());
        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    // ==================== Request Parameter Exceptions ====================

    /**
     * Handles missing required request parameters (400)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex) {
        logger.warn("Missing request parameter: {} of type {}", ex.getParameterName(), ex.getParameterType());
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles type mismatch in path variables or request parameters (400)
     * Example: /recipe/abc when expecting /recipe/123
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        logger.warn("Type mismatch for parameter '{}': expected type {}, got value '{}'",
                ex.getName(), ex.getRequiredType(), ex.getValue());
        String typeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), typeName);
        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    // ==================== HTTP Method & Content Type Exceptions ====================

    /**
     * Handles wrong HTTP method (405)
     * Example: GET request to POST-only endpoint
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        logger.warn("HTTP method not supported: {}. Supported methods: {}",
                ex.getMethod(), ex.getSupportedHttpMethods());
        String message = String.format("HTTP method '%s' not supported. Supported methods: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());
        return buildResponse(message, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles unsupported media type (415)
     * Example: sending XML when only JSON is accepted
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex) {
        logger.warn("Media type not supported: {}. Supported types: {}",
                ex.getContentType(), ex.getSupportedMediaTypes());
        String message = messageProvider.getMessage("error.media.type.not.supported");
        return buildResponse(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handles unacceptable media type in Accept header (406)
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex) {
        logger.warn("Media type not acceptable: {}", ex.getMessage());
        String message = messageProvider.getMessage("error.media.type.not.acceptable");
        return buildResponse(message, HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * Handles invalid JSON format in request body (400)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        logger.warn("Invalid JSON format: {}", ex.getMessage());
        String message = messageProvider.getMessage("json.invalid.format");
        return buildResponse(message, HttpStatus.BAD_REQUEST); // Changed from FORBIDDEN to BAD_REQUEST
    }

    // ==================== Path/URL Exceptions ====================

    /**
     * Handles 404 - No handler found for the URL (404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        logger.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());
        String message = String.format("Endpoint '%s' not found", ex.getRequestURL());
        return buildResponse(message, HttpStatus.NOT_FOUND);
    }

    // ==================== Security Exceptions ====================

    /**
     * Handles authentication failures (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleAuthenticationException(AuthenticationException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        String message = messageProvider.getMessage("error.unauthorized");
        return buildResponse(message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles access denied - insufficient permissions (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        String message = messageProvider.getMessage("error.access.denied");
        return buildResponse(message, HttpStatus.FORBIDDEN);
    }

    // ==================== Database Exceptions ====================

    /**
     * Handles database integrity violations (400)
     * Example: duplicate key, foreign key constraint
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {
        logger.error("Data integrity violation: {}", ex.getMessage());
        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
            String message = messageProvider.getMessage("item.unable.to.delete");
            return buildResponse(message, HttpStatus.BAD_REQUEST);
        }
        return handleGlobalException(ex);
    }

    /**
     * Handles invalid data access API usage (400)
     * Example: invalid JPA query
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            InvalidDataAccessApiUsageException.class
    })
    @ResponseBody
    public ResponseEntity<GenericResponse> handleArgumentException(Exception ex) {
        logger.error("Invalid argument: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ==================== Global Exception Handlers ====================

    /**
     * Handles custom runtime exceptions (500 or custom status)
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleRuntimeException(RuntimeException exp) {
        if (exp instanceof ICustomException) {
            logger.warn("Custom exception: {}", exp.getMessage());
            return buildResponse(exp.getMessage(), ((ICustomException) exp).getStatus());
        }

        logger.error("Runtime exception: {}", exp.getMessage(), exp);
        String message = messageProvider.getMessage("error.internal.server.error");
        return buildResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Global catch-all handler for any unhandled exceptions (500)
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<GenericResponse> handleGlobalException(Exception ex) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        String message = messageProvider.getMessage("error.internal.server.error");
        return buildResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ==================== Utility Methods ====================

    /**
     * Builds standardized error response
     */
    private ResponseEntity<GenericResponse> buildResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(new GenericResponse(message), status);
    }
}
