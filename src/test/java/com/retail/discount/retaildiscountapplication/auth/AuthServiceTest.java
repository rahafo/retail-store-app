package com.retail.discount.retaildiscountapplication.auth;

import com.retail.discount.retaildiscountapplication.auth.dto.AuthenticationRequest;
import com.retail.discount.retaildiscountapplication.auth.dto.AuthenticationResponse;
import com.retail.discount.retaildiscountapplication.auth.dto.RegisterRequest;
import com.retail.discount.retaildiscountapplication.auth.model.User;
import com.retail.discount.retaildiscountapplication.auth.model.UserType;
import com.retail.discount.retaildiscountapplication.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;
    private User user;

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

        user = User.builder()
                .id("1")
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .userType(UserType.REGULAR)
                .registrationDate(LocalDateTime.now())
                .build();
    }

    @Test
    void register_ShouldSaveUser() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Act
        authService.register(registerRequest);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(registerRequest.getName(), savedUser.getName());
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(registerRequest.getUserType(), savedUser.getUserType());
        assertNotNull(savedUser.getRegistrationDate());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already registered", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_ShouldReturnTokenForValidCredentials() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        AuthenticationResponse response = authService.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_WithInvalidUser_ShouldThrowException() {
        // Arrange
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            authService.authenticate(authRequest);
        });

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}