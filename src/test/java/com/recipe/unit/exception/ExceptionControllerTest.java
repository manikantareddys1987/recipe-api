package com.recipe.unit.exception;

import com.recipe.config.MessageProvider;
import com.recipe.exception.ExceptionController;
import com.recipe.exception.ICustomException;
import com.recipe.exception.NotFoundException;
import com.recipe.model.domain.response.GenericResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExceptionController - Global exception handler
 * Tests all exception scenarios and negative cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Exception Controller Unit Tests")
class ExceptionControllerTest {

    @Mock
    private MessageProvider messageProvider;

    private ExceptionController exceptionController;

    @BeforeEach
    void setUp() {
        exceptionController = new ExceptionController(messageProvider);
    }

    // ==================== Custom Application Exceptions ====================

    @Test
    @DisplayName("Should handle NotFoundException with 404 status")
    void testHandleNotFoundException() {
        // Given
        NotFoundException exception = new NotFoundException("Recipe not found");

        // When
        ResponseEntity<GenericResponse> response = exceptionController.handleNotFoundException(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Recipe not found", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle NotFoundException with custom status")
    void testHandleNotFoundExceptionWithCustomStatus() {
        // Given
        NotFoundException exception = new NotFoundException("Resource not found", HttpStatus.GONE);

        // When
        ResponseEntity<GenericResponse> response = exceptionController.handleNotFoundException(exception);

        // Then
        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    // ==================== Validation Exceptions ====================

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with validation errors")
    void testHandleMethodArgumentNotValidException() throws NoSuchMethodException {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("recipe", "name", "Name is required");
        FieldError error2 = new FieldError("recipe", "servings", "Servings must be positive");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        // Create a proper MethodParameter (using any method with a parameter)
        java.lang.reflect.Method method = this.getClass().getDeclaredMethod("dummyMethod", String.class);
        org.springframework.core.MethodParameter parameter = new org.springframework.core.MethodParameter(method, 0);

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleMethodArgumentNotValidException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Name is required"));
        assertTrue(response.getBody().getMessage().contains("Servings must be positive"));
    }

    // Dummy method for MethodParameter creation in tests
    private void dummyMethod(String param) {
        // Used only for reflection in tests
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException")
    void testHandleConstraintViolationException() {
        // Given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("ID must be positive");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        // When
        ResponseEntity<GenericResponse> response = exceptionController.handleConstraintViolation(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ID must be positive", response.getBody().getMessage());
    }

    // ==================== Request Parameter Exceptions ====================

    @Test
    @DisplayName("Should handle MissingServletRequestParameterException")
    void testHandleMissingServletRequestParameter() {
        // Given
        MissingServletRequestParameterException exception =
            new MissingServletRequestParameterException("id", "Integer");

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleMissingServletRequestParameter(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("id"));
        assertTrue(response.getBody().getMessage().contains("missing"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException")
    void testHandleMethodArgumentTypeMismatch() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        when(exception.getValue()).thenReturn("abc");
        when(exception.getRequiredType()).thenReturn((Class) Integer.class);

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleMethodArgumentTypeMismatch(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("abc"));
        assertTrue(response.getBody().getMessage().contains("id"));
        assertTrue(response.getBody().getMessage().contains("Integer"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException with null type")
    void testHandleMethodArgumentTypeMismatchNullType() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        when(exception.getValue()).thenReturn("abc");
        when(exception.getRequiredType()).thenReturn(null);

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleMethodArgumentTypeMismatch(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("unknown"));
    }

    // ==================== HTTP Method & Content Type Exceptions ====================

    @Test
    @DisplayName("Should handle HttpRequestMethodNotSupportedException")
    void testHandleHttpRequestMethodNotSupported() {
        // Given
        HttpRequestMethodNotSupportedException exception =
            new HttpRequestMethodNotSupportedException("GET", List.of("POST", "PUT"));

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleHttpRequestMethodNotSupported(exception);

        // Then
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("GET"));
        assertTrue(response.getBody().getMessage().contains("not supported"));
    }

    @Test
    @DisplayName("Should handle HttpMediaTypeNotSupportedException")
    void testHandleHttpMediaTypeNotSupported() {
        // Given
        when(messageProvider.getMessage("error.media.type.not.supported"))
            .thenReturn("Media type not supported. Please use application/json.");

        HttpMediaTypeNotSupportedException exception =
            new HttpMediaTypeNotSupportedException("text/xml is not supported");

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleHttpMediaTypeNotSupported(exception);

        // Then
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Media type not supported. Please use application/json.",
            response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle HttpMediaTypeNotAcceptableException")
    void testHandleHttpMediaTypeNotAcceptable() {
        // Given
        when(messageProvider.getMessage("error.media.type.not.acceptable"))
            .thenReturn("Requested media type is not acceptable");

        HttpMediaTypeNotAcceptableException exception =
            new HttpMediaTypeNotAcceptableException("Media type not acceptable");

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleHttpMediaTypeNotAcceptable(exception);

        // Then
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Requested media type is not acceptable", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException - Invalid JSON")
    void testHandleHttpMessageNotReadableException() {
        // Given
        when(messageProvider.getMessage("json.invalid.format"))
            .thenReturn("Invalid JSON format. Please check your request body.");

        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleHttpMessageNotReadableException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid JSON format. Please check your request body.",
            response.getBody().getMessage());
    }

    // ==================== Path/URL Exceptions ====================

    @Test
    @DisplayName("Should handle NoHandlerFoundException - 404")
    void testHandleNoHandlerFoundException() {
        // Given
        NoHandlerFoundException exception =
            new NoHandlerFoundException("GET", "/api/invalid", null);

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleNoHandlerFoundException(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("/api/invalid"));
        assertTrue(response.getBody().getMessage().contains("not found"));
    }

    // ==================== Security Exceptions ====================

    @Test
    @DisplayName("Should handle AuthenticationException - 401")
    void testHandleAuthenticationException() {
        // Given
        when(messageProvider.getMessage("error.unauthorized"))
            .thenReturn("Authentication required. Please provide valid credentials.");

        AuthenticationException exception = mock(AuthenticationException.class);

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleAuthenticationException(exception);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Authentication required. Please provide valid credentials.",
            response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle AccessDeniedException - 403")
    void testHandleAccessDeniedException() {
        // Given
        when(messageProvider.getMessage("error.access.denied"))
            .thenReturn("Access denied. You don't have permission to access this resource.");

        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleAccessDeniedException(exception);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied. You don't have permission to access this resource.",
            response.getBody().getMessage());
    }

    // ==================== Database Exceptions ====================

    @Test
    @DisplayName("Should handle DataIntegrityViolationException - Constraint violation")
    void testHandleDataIntegrityViolationExceptionWithConstraint() {
        // Given
        when(messageProvider.getMessage("item.unable.to.delete"))
            .thenReturn("Unable to delete item. It may have dependent entities.");

        org.hibernate.exception.ConstraintViolationException cause =
            mock(org.hibernate.exception.ConstraintViolationException.class);
        DataIntegrityViolationException exception =
            new DataIntegrityViolationException("Constraint violation", cause);

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleDataIntegrityViolationException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unable to delete item. It may have dependent entities.",
            response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolationException - Other causes")
    void testHandleDataIntegrityViolationExceptionOtherCause() {
        // Given
        when(messageProvider.getMessage("error.internal.server.error"))
            .thenReturn("Internal server error. Please try again later.");

        DataIntegrityViolationException exception =
            new DataIntegrityViolationException("Data error");

        // When
        ResponseEntity<GenericResponse> response =
            exceptionController.handleDataIntegrityViolationException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void testHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<GenericResponse> response = exceptionController.handleArgumentException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle InvalidDataAccessApiUsageException")
    void testHandleInvalidDataAccessApiUsageException() {
        // Given
        InvalidDataAccessApiUsageException exception =
            new InvalidDataAccessApiUsageException("Invalid query");

        // When
        ResponseEntity<GenericResponse> response = exceptionController.handleArgumentException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid query", response.getBody().getMessage());
    }

    // ==================== Global Exception Handlers ====================

    @Test
    @DisplayName("Should handle custom RuntimeException with ICustomException")
    void testHandleCustomRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Custom error") {
        };
        ICustomException customException = (ICustomException) () -> HttpStatus.CONFLICT;
        RuntimeException combinedException = new RuntimeException("Custom error") {
        };

        // Create a proper custom exception
        class TestCustomException extends RuntimeException implements ICustomException {
            public TestCustomException(String message) {
                super(message);
            }

            @Override
            public HttpStatus getStatus() {
                return HttpStatus.CONFLICT;
            }
        }

        TestCustomException testException = new TestCustomException("Custom conflict");

        // When
        ResponseEntity<GenericResponse> response = exceptionController.handleRuntimeException(testException);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Custom conflict", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle generic RuntimeException")
    void testHandleGenericRuntimeException() {
        // Given
        when(messageProvider.getMessage("error.internal.server.error"))
            .thenReturn("Internal server error. Please try again later.");

        RuntimeException exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<GenericResponse> response = exceptionController.handleRuntimeException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error. Please try again later.",
            response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle any unhandled Exception")
    void testHandleGlobalException() {
        // Given
        when(messageProvider.getMessage("error.internal.server.error"))
            .thenReturn("Internal server error. Please try again later.");

        Exception exception = new Exception("Unexpected exception");

        // When
        ResponseEntity<GenericResponse> response = exceptionController.handleGlobalException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error. Please try again later.",
            response.getBody().getMessage());
    }
}
