package com.makeyourjurney.application.auth;

import com.makeyourjurney.infrastructure.persistence.UserEntity;
import com.makeyourjurney.infrastructure.persistence.UserJpaRepository;
import com.makeyourjurney.infrastructure.security.JwtService;
import com.makeyourjurney.infrastructure.security.RefreshTokenStore;
import com.makeyourjurney.presentation.dto.request.LoginRequest;
import com.makeyourjurney.presentation.dto.request.RegisterRequest;
import com.makeyourjurney.presentation.dto.response.AuthResponse;
import com.makeyourjurney.presentation.dto.response.MeResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    public AuthService(
            UserJpaRepository userJpaRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenStore refreshTokenStore
    ) {
        this.userJpaRepository = userJpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userJpaRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }
        var user = new UserEntity(
                UUID.randomUUID().toString(), request.email(),
                passwordEncoder.encode(request.password()), request.name(), Instant.now()
        );
        userJpaRepository.save(user);
        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        var user = userJpaRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Email atau password salah"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Email atau password salah");
        }
        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        String userId = refreshTokenStore.resolve(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token tidak valid"));
        refreshTokenStore.revoke(refreshToken);
        var user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        return issueTokens(user);
    }

    public void logout(String refreshToken) {
        refreshTokenStore.revoke(refreshToken);
    }

    public MeResponse me(String userId) {
        var user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        return new MeResponse(user.getId(), user.getEmail(), user.getName());
    }

    private AuthResponse issueTokens(UserEntity user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = refreshTokenStore.issue(user.getId());
        return new AuthResponse(accessToken, refreshToken);
    }
}
