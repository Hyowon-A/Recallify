package com.andy.recallify.user.controller;

import com.andy.recallify.user.service.UserService;
import com.andy.recallify.user.dto.LoginRequest;
import com.andy.recallify.user.dto.ResetPasswordRequest;
import com.andy.recallify.user.dto.UpdateUserRequest;
import com.andy.recallify.user.dto.VerifyResetCodeRequest;
import com.andy.recallify.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.andy.recallify.security.JwtUtil;

import java.util.HashMap;
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
    public ResponseEntity<?> addNewUser(@RequestBody User user) {
        try {
            User registered = userService.addNewUser(user);
            String token = jwtUtil.generateToken(registered.getEmail());

            return ResponseEntity.ok().body(Map.of(
                    "token", token,
                    "name", registered.getName(),
                    "email", registered.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());
            String token = jwtUtil.generateToken(request.getEmail());

            return ResponseEntity.ok().body(Map.of(
                    "token", token,
                    "name", user.getName(),
                    "email", user.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> updateUser(
            @RequestBody UpdateUserRequest updateUserRequest,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            User updatedUser = userService.updateUser(email, updateUserRequest);

            String updatedToken = jwtUtil.generateToken(updatedUser.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("name", updatedUser.getName());
            response.put("email", updatedUser.getEmail());
            response.put("token", updatedToken);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/sendResetCode")
    public ResponseEntity<?> sendResetCode(@RequestBody String email) {
        try {
            userService.sendResetCode(email);
            return ResponseEntity.ok().body(Map.of("success", "Reset code sent to " + email));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verifyResetCode")
    public ResponseEntity<?> verifyResetCode(@RequestBody VerifyResetCodeRequest verifyResetCodeRequest) {
        try {
            userService.verifyResetCode(verifyResetCodeRequest.email(), verifyResetCodeRequest.code());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            userService.resetPassword(resetPasswordRequest.email(), resetPasswordRequest.newPassword());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return  ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
