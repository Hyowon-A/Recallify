package com.andy.recallify.features.user.repository;

import com.andy.recallify.features.user.model.PasswordResetCode;
import com.andy.recallify.features.user.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findTopByUserOrderByCreatedAtDesc(User user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteByUser(User user);
}
