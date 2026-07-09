package com.makeyourjurney.application.auth;

import com.makeyourjurney.infrastructure.persistence.UserEntity;
import com.makeyourjurney.infrastructure.persistence.UserJpaRepository;
import com.makeyourjurney.infrastructure.security.JwtService;
import com.makeyourjurney.infrastructure.security.RefreshTokenStore;
import com.makeyourjurney.presentation.dto.request.LoginRequest;
import com.makeyourjurney.presentation.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenStore refreshTokenStore;

    private AuthService authService() {
        return new AuthService(userJpaRepository, passwordEncoder, jwtService, refreshTokenStore);
    }

    @Test
    void register_newEmail_savesUserAndIssuesTokens() {
        when(userJpaRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("access-token");
        when(refreshTokenStore.issue(anyString())).thenReturn("refresh-token");

        var response = authService().register(new RegisterRequest("new@example.com", "password123", "New User"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(userJpaRepository).save(any(UserEntity.class));
    }

    @Test
    void register_existingEmail_throws() {
        when(userJpaRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService().register(new RegisterRequest("taken@example.com", "password123", "X")))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void login_correctPassword_issuesTokens() {
        var user = new UserEntity("user-1", "user@example.com", "hashed", "User", Instant.now());
        when(userJpaRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("access-token");
        when(refreshTokenStore.issue(anyString())).thenReturn("refresh-token");

        var response = authService().login(new LoginRequest("user@example.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
    }

    @Test
    void login_wrongPassword_throws() {
        var user = new UserEntity("user-1", "user@example.com", "hashed", "User", Instant.now());
        when(userJpaRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService().login(new LoginRequest("user@example.com", "wrong")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refresh_validToken_rotatesAndIssuesNewTokens() {
        var user = new UserEntity("user-1", "user@example.com", "hashed", "User", Instant.now());
        when(refreshTokenStore.resolve("old-refresh")).thenReturn(Optional.of("user-1"));
        when(userJpaRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("new-access-token");
        when(refreshTokenStore.issue(anyString())).thenReturn("new-refresh-token");

        var response = authService().refresh("old-refresh");

        verify(refreshTokenStore).revoke("old-refresh");
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void refresh_unknownToken_throws() {
        when(refreshTokenStore.resolve("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService().refresh("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void logout_revokesRefreshToken() {
        authService().logout("some-refresh");

        verify(refreshTokenStore).revoke("some-refresh");
    }
}
