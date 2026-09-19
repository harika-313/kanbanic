package com.kanbanic.auth_service.service;

import com.kanbanic.auth_service.dto.LoginRequest;
import com.kanbanic.auth_service.dto.SignupRequest;
import com.kanbanic.auth_service.entity.User;
import com.kanbanic.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Runs before every test — keeps each test starting fresh
    }

    @Test
    void signup_shouldSaveNewUser_whenEmailNotTaken() {
        SignupRequest request = new SignupRequest();
        request.setEmail("new@kanbanic.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("new@kanbanic.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.signup(request);

        assertEquals("new@kanbanic.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signup_shouldThrowException_whenEmailAlreadyExists() {
        SignupRequest request = new SignupRequest();
        request.setEmail("existing@kanbanic.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("existing@kanbanic.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldSucceed_whenCredentialsAreCorrect() {
        String rawPassword = "correctPassword";
        User existingUser = new User();
        existingUser.setEmail("user@kanbanic.com");
        existingUser.setPassword(encoder.encode(rawPassword));

        LoginRequest request = new LoginRequest();
        request.setEmail("user@kanbanic.com");
        request.setPassword(rawPassword);

        when(userRepository.findByEmail("user@kanbanic.com")).thenReturn(Optional.of(existingUser));

        User result = authService.login(request);

        assertEquals("user@kanbanic.com", result.getEmail());
    }

    @Test
    void login_shouldThrowException_whenPasswordIsWrong() {
        User existingUser = new User();
        existingUser.setEmail("user@kanbanic.com");
        existingUser.setPassword(encoder.encode("correctPassword"));

        LoginRequest request = new LoginRequest();
        request.setEmail("user@kanbanic.com");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("user@kanbanic.com")).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_shouldThrowException_whenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@kanbanic.com");
        request.setPassword("anyPassword");

        when(userRepository.findByEmail("ghost@kanbanic.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }
}