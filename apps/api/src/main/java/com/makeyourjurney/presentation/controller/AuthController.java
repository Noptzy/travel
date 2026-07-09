package com.makeyourjurney.presentation.controller;

import com.makeyourjurney.application.auth.AuthService;
import com.makeyourjurney.presentation.dto.request.LoginRequest;
import com.makeyourjurney.presentation.dto.request.RefreshRequest;
import com.makeyourjurney.presentation.dto.request.RegisterRequest;
import com.makeyourjurney.presentation.dto.response.AuthResponse;
import com.makeyourjurney.presentation.dto.response.MeResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        return authService.me((String) authentication.getPrincipal());
    }
}
