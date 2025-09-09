package com.andy.recallify.user;

public record VerifyResetCodeRequest(String email, String code) {
}
