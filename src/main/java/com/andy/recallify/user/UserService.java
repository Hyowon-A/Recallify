package com.andy.recallify.user;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;

    private final Mailer mailer;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final Duration RESET_CODE_TTL = Duration.ofMinutes(5);
    private static final int CODE_LEN = 6;

    @Autowired
    public UserService(UserRepository userRepository, PasswordResetCodeRepository passwordResetCodeRepository, Mailer mailer) {
        this.userRepository = userRepository;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.mailer = mailer;
    }

    public static String hashPassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    public static boolean matches(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }

    public User addNewUser(User user) {
        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());
        if (userOptional.isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        user.setPassword(hashPassword(user.getPassword()));
        userRepository.save(user);
        return user;
    }

    public User login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!matches(password, user.getPassword())) {
                throw new IllegalArgumentException("Wrong password");
            }
            return user;
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Transactional
    public User updateUser(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.email() != null && !request.email().trim().isEmpty()) {
            String newEmail = request.email().trim();
            if (!newEmail.equals(user.getEmail()) && userRepository.findByEmail(newEmail).isPresent()) {
                throw new IllegalArgumentException("Email is already registered.");
            }
            user.setEmail(newEmail);
        }

        if (request.name() != null && !request.name().trim().isEmpty()) {
            user.setName(request.name().trim());
        }

        if (request.newPassword() != null && !request.newPassword().trim().isEmpty()) {
            if (request.currentPassword() == null || request.currentPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("Current password is required to change password.");
            }

            if (!matches(request.currentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Incorrect current password.");
            }

            user.setPassword(hashPassword(request.newPassword().trim()));
        }
        userRepository.save(user);
        return user;
    }

    @Transactional
    public void sendResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // delete old reset codes for this user
        passwordResetCodeRepository.deleteByUser(user);

        String code = generateNumericCode(CODE_LEN);
        var prc = new PasswordResetCode();
        prc.setUser(user);
        prc.setCode(code);
        prc.setExpiresAt(Instant.now().plus(RESET_CODE_TTL));
        prc.setUsed(false);
        passwordResetCodeRepository.save(prc);

        mailer.send(
                user.getEmail(),
                "Recallify - Password Reset Code",
                """
                Hi %s,
        
                Your password reset code is: %s
                It expires in %d minutes.
        
                If you didn't request this, please ignore this email.
                """.formatted(user.getName(), code, RESET_CODE_TTL.toMinutes())
        );

    }

    private String generateNumericCode(int len) {
        var rnd = new SecureRandom();
        int n = (int)Math.pow(10, len);
        return String.format("%0" + len + "d", rnd.nextInt(n));
    }

    @Transactional
    public void verifyResetCode(String email, String code) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String normalizedCode  = code == null ? "" : code.trim();

        if (!normalizedCode.matches("\\d{6}")) {
            throw new IllegalArgumentException("Invalid code format");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        PasswordResetCode resetCode = passwordResetCodeRepository.findByUserAndCode(user, normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired code"));

        if (resetCode.isUsed() || resetCode.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Invalid or expired code");
        }

        // mark as used (single use)
        resetCode.setUsed(true);
    }
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(hashPassword(newPassword.trim()));
    }
}
