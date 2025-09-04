package com.smartDine.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartDine.dto.LoginUserDTO;
import com.smartDine.dto.RegisterUserDTO;
import com.smartDine.entity.User;
import com.smartDine.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterUserDTO registerUserDTO;
    private LoginUserDTO loginUserDTO;

    /**
     * Set up method to initialize common objects before each test.
     */
    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john.doe@test.com", "encodedPassword123", 123456789L);
        testUser.setId(1L);

        registerUserDTO = new RegisterUserDTO("John Doe", "john.doe@test.com", "password123", 123456789L);
        loginUserDTO = new LoginUserDTO("john.doe@test.com", "password123");
    }

    /**
     * Tests successful user registration scenario.
     */
    @Test
    void signup_CorrectRegistration_ReturnsUser() {
        // Arrange
        User userToSave = new User();
        userToSave.setName("John Doe");
        userToSave.setEmail("john.doe@test.com");
        userToSave.setPassword("encodedPassword123");
        userToSave.setNumber(123456789L);

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(usersRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User createdUser = authService.signup(registerUserDTO);

        // Assert
        assertNotNull(createdUser);
        assertEquals("John Doe", createdUser.getName());
        assertEquals("john.doe@test.com", createdUser.getEmail());
        assertEquals("encodedPassword123", createdUser.getPassword());
        assertEquals(123456789L, createdUser.getNumber());
        assertEquals(1L, createdUser.getId());

        verify(passwordEncoder, times(1)).encode("password123");
        verify(usersRepository, times(1)).save(any(User.class));
    }

    /**
     * Tests registration scenario when email or phone already exists (database constraint violation).
     */
    @Test
    void signup_DuplicateEmailOrPhone_ThrowsDataIntegrityViolationException() {
        // Arrange
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(usersRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> 
            authService.signup(registerUserDTO));

        verify(passwordEncoder, times(1)).encode("password123");
        verify(usersRepository, times(1)).save(any(User.class));
    }

    /**
     * Tests registration with null or empty password.
     */
    @Test
    void signup_NullPassword_HandledGracefully() {
        // Arrange
        RegisterUserDTO invalidDto = new RegisterUserDTO("John Doe", "john.doe@test.com", null, 123456789L);
        when(passwordEncoder.encode(null)).thenThrow(IllegalArgumentException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            authService.signup(invalidDto));

        verify(passwordEncoder, times(1)).encode(null);
        verify(usersRepository, never()).save(any(User.class));
    }

    /**
     * Tests successful authentication scenario.
     */
    @Test
    void authenticate_ValidCredentials_ReturnsUser() {
        // Arrange
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken("john.doe@test.com", "password123");
        
        // No need to mock authenticate() since it returns void - just don't throw exception
        when(usersRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));

        // Act
        User authenticatedUser = authService.authenticate(loginUserDTO);

        // Assert
        assertNotNull(authenticatedUser);
        assertEquals("John Doe", authenticatedUser.getName());
        assertEquals("john.doe@test.com", authenticatedUser.getEmail());
        assertEquals(1L, authenticatedUser.getId());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usersRepository, times(1)).findByEmail("john.doe@test.com");
    }

    /**
     * Tests authentication scenario with invalid credentials.
     */
    @Test
    void authenticate_InvalidCredentials_ThrowsBadCredentialsException() {
        // Arrange
        doThrow(BadCredentialsException.class)
            .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> 
            authService.authenticate(loginUserDTO));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usersRepository, never()).findByEmail(anyString());
    }

    /**
     * Tests authentication scenario when user is not found after successful authentication.
     */
    @Test
    void authenticate_UserNotFoundAfterAuth_ThrowsRuntimeException() {
        // Arrange
        // AuthenticationManager.authenticate() succeeds (no exception thrown)
        when(usersRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            authService.authenticate(loginUserDTO));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usersRepository, times(1)).findByEmail("john.doe@test.com");
    }

    /**
     * Tests authentication with null email.
     */
    @Test
    void authenticate_NullEmail_ThrowsException() {
        // Arrange
        LoginUserDTO invalidLoginDto = new LoginUserDTO(null, "password123");
        
        doThrow(IllegalArgumentException.class)
            .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            authService.authenticate(invalidLoginDto));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usersRepository, never()).findByEmail(anyString());
    }

    /**
     * Tests authentication with empty password.
     */
    @Test
    void authenticate_EmptyPassword_ThrowsBadCredentialsException() {
        // Arrange
        LoginUserDTO emptyPasswordDto = new LoginUserDTO("john.doe@test.com", "");
        
        doThrow(BadCredentialsException.class)
            .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> 
            authService.authenticate(emptyPasswordDto));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usersRepository, never()).findByEmail(anyString());
    }

    /**
     * Tests that password encoding is called with correct parameters during signup.
     */
    @Test
    void signup_PasswordEncodingVerification() {
        // Arrange
        String rawPassword = "mySecretPassword";
        String encodedPassword = "encodedMySecretPassword";
        
        RegisterUserDTO customDto = new RegisterUserDTO("Test User", "test@example.com", rawPassword, 999888777L);
        User savedUser = new User("Test User", "test@example.com", encodedPassword, 999888777L);
        savedUser.setId(2L);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(usersRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = authService.signup(customDto);

        // Assert
        assertEquals(encodedPassword, result.getPassword());
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    /**
     * Tests that authentication token is created with correct username and password.
     */
    @Test
    void authenticate_AuthenticationTokenVerification() {
        // Arrange
        // AuthenticationManager.authenticate() succeeds (no exception thrown)
        when(usersRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));

        // Act
        authService.authenticate(loginUserDTO);

        // Assert
        verify(authenticationManager, times(1)).authenticate(
            argThat(token -> 
                token instanceof UsernamePasswordAuthenticationToken &&
                "john.doe@test.com".equals(token.getPrincipal()) &&
                "password123".equals(token.getCredentials())
            )
        );
    }
}
