package com.minibank.authservice.Repository;

import com.minibank.authservice.Entity.RefreshToken;
import com.minibank.authservice.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(Users user);
    void deleteByToken(String token);
}
