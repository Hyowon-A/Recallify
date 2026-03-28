package com.andy.recallify.features.set.dto;

public record PublicSetDto(Long id, String title, boolean isPublic, int count,
                           String type, boolean isOwner) {
}
