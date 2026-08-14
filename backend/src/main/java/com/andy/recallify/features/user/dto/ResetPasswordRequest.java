package com.andy.recallify.features.user.dto;

public record ResetPasswordRequest(String email, String code, String newPassword) {
}
