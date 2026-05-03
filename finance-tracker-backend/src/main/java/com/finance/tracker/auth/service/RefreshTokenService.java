package com.finance.tracker.auth.service;

import com.finance.tracker.auth.domain.entity.RefreshToken;
import com.finance.tracker.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    public RefreshToken create(UUID userId) {
        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiryDate(OffsetDateTime.now().plusDays(30));
        return repo.save(rt);
    }

    public RefreshToken verify(String token) {
        RefreshToken rt = repo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (rt.getExpiryDate().isBefore(OffsetDateTime.now())) {
            repo.delete(rt);
            throw new RuntimeException("Expired refresh token");
        }

        return rt;
    }

    public void delete(String token) {
        repo.deleteByToken(token);
    }
}