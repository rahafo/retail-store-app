package com.retail.discount.retaildiscountapplication.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleApiException_ShouldReturnCorrectResponse() {
        // Arrange
        ApiException apiException = new ApiException("Test error message", HttpStatus.BAD_REQUEST);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleApiException(apiException);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Test error message", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleValidationException_ShouldReturnCorrectResponse() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("object", "field1", "Field 1 error"),
                new FieldError("object", "field2", "Field 2 error")
        ));

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(2, response.getBody().getErrors().size());
        assertEquals("field1: Field 1 error", response.getBody().getErrors().get(0));
        assertEquals("field2: Field 2 error", response.getBody().getErrors().get(1));
    }
}