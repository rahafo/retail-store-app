package com.retail.discount.retaildiscountapplication.auth;

import com.retail.discount.retaildiscountapplication.auth.model.User;
import com.retail.discount.retaildiscountapplication.auth.model.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    @Spy
    @InjectMocks
    private JwtUtil jwtUtil;
    private User userDetails;
    private String token;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secretKey", SECRET_KEY);

        userDetails = User.builder()
                .id("1")
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .userType(UserType.REGULAR)
                .registrationDate(LocalDateTime.now())
                .build();

        // Generate a real token for testing
        doReturn(getSigningKey()).when(jwtUtil).getSigningKey();
        token = jwtUtil.generateToken(userDetails);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Act
        String generatedToken = jwtUtil.generateToken(userDetails);

        // Assert
        assertNotNull(generatedToken);

        // Verify the token can be parsed
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(generatedToken)
                .getBody();

        assertEquals(userDetails.getUsername(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void extractUsername_ShouldReturnCorrectEmail() {
        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals(userDetails.getEmail(), username);
    }

    @Test
    void validateToken_WithValidTokenAndMatchingUser_ShouldReturnTrue() {
        // Act
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithValidTokenButDifferentUser_ShouldReturnFalse() {
        // Arrange
        User differentUser = User.builder()
                .email("different@example.com")
                .build();

        // Act
        boolean isValid = jwtUtil.validateToken(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        // Create a token that's already expired
        String expiredToken = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 30))
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60))
                .signWith(getSigningKey())
                .compact();

        // Act & Assert
        assertFalse(jwtUtil.validateToken(expiredToken, userDetails));
    }

    // Helper method to create signing key
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}