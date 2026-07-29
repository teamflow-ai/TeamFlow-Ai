package com.teamflow.ai.auth.repository;

import com.teamflow.ai.auth.entity.PasswordResetToken;
import com.teamflow.ai.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for performing CRUD and lookup operations on {@link PasswordResetToken} entities.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);

    void deleteByToken(String token);
}
