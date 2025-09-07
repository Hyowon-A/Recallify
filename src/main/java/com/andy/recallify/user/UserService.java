package com.andy.recallify.user;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

}
