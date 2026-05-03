package com.finance.tracker.auth.service;

import com.finance.tracker.auth.domain.LoginRequest;
import com.finance.tracker.auth.domain.RefreshRequest;
import com.finance.tracker.auth.domain.RegisterRequest;
import com.finance.tracker.auth.domain.dtos.AuthResponse;
import com.finance.tracker.auth.domain.entity.User;

import java.util.Optional;

public interface AuthService {

    AuthResponse login(LoginRequest req);
    AuthResponse register(RegisterRequest req);
    AuthResponse refresh(RefreshRequest req);
    void logout(RefreshRequest req);
    Optional<User> findByEmail(String email);
}
