package com.andy.recallify.user;

public record ResetPasswordRequest(String email, String newPassword) {
}
