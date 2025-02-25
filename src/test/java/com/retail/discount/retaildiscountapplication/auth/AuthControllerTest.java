package com.retail.discount.retaildiscountapplication.auth;

import com.retail.discount.retaildiscountapplication.auth.dto.AuthenticationRequest;
import com.retail.discount.retaildiscountapplication.auth.dto.AuthenticationResponse;
import com.retail.discount.retaildiscountapplication.auth.dto.RegisterRequest;
import com.retail.discount.retaildiscountapplication.auth.model.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;
    private AuthenticationResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");
        registerRequest.setUserType(UserType.REGULAR);

        authRequest = new AuthenticationRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password");

        authResponse = AuthenticationResponse.builder()
                .token("jwt-token")
                .build();
    }

    @Test
    void register_ShouldReturnCreatedStatus() {
        // Arrange
        doNothing().when(authService).register(any(RegisterRequest.class));

        // Act
        ResponseEntity<Void> response = authController.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authService).register(registerRequest);
    }

    @Test
    void authenticate_ShouldReturnToken() {
        // Arrange
        when(authService.authenticate(any(AuthenticationRequest.class))).thenReturn(authResponse);

        // Act
        ResponseEntity<AuthenticationResponse> response = authController.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
    }
}