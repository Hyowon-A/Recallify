package com.andy.recallify.user.dto;

public record VerifyResetCodeRequest(String email, String code) {
}
