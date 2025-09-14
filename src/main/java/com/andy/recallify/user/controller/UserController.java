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
            String accessToken = jwtUtil.generateAccessToken(registered.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(registered.getEmail());
            userService.saveRefreshToken(registered.getEmail(), refreshToken);

            return ResponseEntity.ok().body(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
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

            String accessToken = jwtUtil.generateAccessToken(user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
            userService.saveRefreshToken(user.getEmail(), refreshToken);

            return ResponseEntity.ok().body(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
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

            String updatedAccessToken = jwtUtil.generateAccessToken(updatedUser.getEmail());
            String updatedRefreshToken = jwtUtil.generateRefreshToken(updatedUser.getEmail());
            userService.saveRefreshToken(updatedUser.getEmail(), updatedRefreshToken);

            return ResponseEntity.ok(Map.of(
                    "accessToken", updatedAccessToken,
                    "refreshToken", updatedRefreshToken,
                    "name", updatedUser.getName(),
                    "email", updatedUser.getEmail()
            ));
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

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> payload) {
        try {
            String refreshToken = payload.get("refreshToken");
            String email = jwtUtil.extractEmail(refreshToken);

            if (!jwtUtil.validateToken(refreshToken) || !userService.isRefreshTokenValid(email, refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid refresh token"));
            }

            String newAccessToken = jwtUtil.generateAccessToken(email);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token refresh failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);
            userService.invalidateRefreshToken(email);

            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
