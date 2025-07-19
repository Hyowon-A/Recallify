package com.andy.recallify.user;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addNewUser(User user) {
        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());
        if (userOptional.isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long userId, String email, String name, String password) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (email != null && !email.isEmpty() && !user.getEmail().equals(email)) {
            userRepository.findByEmail(email)
                    .ifPresent(u -> { throw new IllegalArgumentException("User with " + email + " already exists"); });
            user.setEmail(email);
        }

        if (name != null && !name.isEmpty() && !name.equals(user.getName())) {
            user.setName(name);
        }

        if (password != null && !password.isEmpty() && !password.equals(user.getPassword())) {
            user.setPassword(password);
        }

    }

}
