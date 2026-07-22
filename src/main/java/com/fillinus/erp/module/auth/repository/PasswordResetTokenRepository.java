package com.fillinus.erp.module.auth.repository;

import com.fillinus.erp.module.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    /** Invalidate all previous unused tokens for a user before issuing a new one */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.isUsed = true WHERE t.user.id = :userId AND t.isUsed = false")
    void invalidateAllByUserId(Long userId);
}
