package com.yessinCoding.repository;

import com.yessinCoding.entity.RefreshToken;
import com.yessinCoding.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteByUser(User user);
    Optional<RefreshToken> findByUser(User user);
}
