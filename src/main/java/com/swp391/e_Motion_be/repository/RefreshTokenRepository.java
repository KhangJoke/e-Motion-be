package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.RefreshToken;
import com.swp391.e_Motion_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserAndRevokedFalse(User user);
    void deleteByUser(User user);
}
