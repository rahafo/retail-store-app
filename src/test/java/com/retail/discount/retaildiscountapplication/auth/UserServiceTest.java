package com.retail.discount.retaildiscountapplication.auth;

import com.retail.discount.retaildiscountapplication.auth.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsername_WithExistingUser_ShouldReturnUser() {
        // Arrange
        User expectedUser = User.builder().email("test@example.com").build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(expectedUser));

        // Act
        User actualUser = (User) userService.loadUserByUsername("test@example.com");

        // Assert
        assertEquals(expectedUser, actualUser);
    }

    @Test
    void loadUserByUsername_WithNonExistingUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent@example.com");
        });

        assertTrue(exception.getMessage().contains("User not found with email"));
    }
}