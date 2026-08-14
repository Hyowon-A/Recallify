package com.andy.recallify.features.user.dto;

public record UpdateUserRequest(String name, String email, String currentPassword, String newPassword) {
}
