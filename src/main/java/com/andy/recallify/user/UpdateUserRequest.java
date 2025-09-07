package com.andy.recallify.user;

public record UpdateUserRequest(String name, String email, String currentPassword, String newPassword) {
}
