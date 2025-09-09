package com.andy.recallify.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findByUserAndCode(User user, String code);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteByUser(User user);
}
