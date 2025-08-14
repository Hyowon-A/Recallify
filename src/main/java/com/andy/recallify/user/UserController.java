package com.andy.recallify.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.andy.recallify.security.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping(path = "api/user")
public class UserController {

    private final UserService userService;

    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public void addNewUser(@RequestBody User user) {
        userService.addNewUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            userService.login(request.getEmail(), request.getPassword());
            String token = jwtUtil.generateToken(request.getEmail());

            return ResponseEntity.ok().body(Map.of("token", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PutMapping(path = "{userId}")
    public void updateUser(@PathVariable("userId") Long userId,
                           @RequestBody UpdateUserRequest request) {
        userService.updateUser(userId, request);
    }
}
