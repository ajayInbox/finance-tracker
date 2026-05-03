package com.finance.tracker.auth.controller;

import com.finance.tracker.auth.domain.LoginRequest;
import com.finance.tracker.auth.domain.RefreshRequest;
import com.finance.tracker.auth.domain.RegisterRequest;
import com.finance.tracker.auth.domain.dtos.AuthResponse;
import com.finance.tracker.auth.service.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest req) {
       return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest req) {
        authService.logout(req);
        return ResponseEntity.ok().build();
    }
}