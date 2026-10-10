package com.tamaka.tourism.repository;

import com.tamaka.tourism.entity.Token;
import com.tamaka.tourism.entity.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    Optional<Token> findByUserIdAndTokenTypeAndUsedAtIsNull(Long userId, TokenType tokenType);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}