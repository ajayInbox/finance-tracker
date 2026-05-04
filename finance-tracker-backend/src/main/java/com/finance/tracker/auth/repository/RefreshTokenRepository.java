package com.finance.tracker.auth.repository;

import com.finance.tracker.auth.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.token = :token")
    void deleteByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
