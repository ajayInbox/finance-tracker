package com.finance.tracker.auth.service;

import com.finance.tracker.auth.domain.LoginRequest;
import com.finance.tracker.auth.domain.RefreshRequest;
import com.finance.tracker.auth.domain.RegisterRequest;
import com.finance.tracker.auth.domain.UserResponse;
import com.finance.tracker.auth.domain.dtos.AuthResponse;
import com.finance.tracker.auth.domain.entity.RefreshToken;
import com.finance.tracker.auth.domain.entity.User;
import com.finance.tracker.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.tracker.accounts.domain.AccountCategory;
import com.finance.tracker.accounts.domain.AccountStatus;
import com.finance.tracker.accounts.domain.AccountType;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.repository.AccountRepository;
import com.finance.tracker.category.domain.CategoryType;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.category.repository.CategoryRepository;
import com.finance.tracker.transactions.domain.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshService;
    private final AuthenticationManager authManager;
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;

    @Override
    public AuthResponse login(LoginRequest req) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        User user = (User) authentication.getPrincipal();

        refreshService.deleteTokenForUser(user.getId());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshService.create(user.getId()).getToken();

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse register(RegisterRequest req) {

        // 1. Check if user exists
        if (userRepo.findByEmail(req.email()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // 2. Create user
        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setName(req.name());
        user.setRole("ROLE_USER");

        userRepo.save(user);

        // 3. Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshService.create(user.getId()).getToken();

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken rt = refreshService.verify(req.refreshToken());
        User user = userRepo.findById(rt.getUserId()).orElseThrow();
        String newAccess = jwtService.generateAccessToken(user);
        return new AuthResponse(newAccess, rt.getToken());
    }

    @Override
    public void logout(RefreshRequest req) {
        refreshService.delete(req.refreshToken());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    @Override
    public UserResponse getUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(
                () -> new RuntimeException("User not found")
        );
        return new UserResponse(user.getName(), user.getEmail(), true);
    }
}