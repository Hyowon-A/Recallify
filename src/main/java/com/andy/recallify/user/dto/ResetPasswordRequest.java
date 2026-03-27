package com.andy.recallify.user.dto;

public record ResetPasswordRequest(String email, String code, String newPassword) {
}
