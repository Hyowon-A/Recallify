package com.andy.recallify.user.dto;

public record UpdateUserRequest(String name, String email, String currentPassword, String newPassword) {
}
