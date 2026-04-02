package com.andy.recallify.features.set.dto;

public record CreateSetRequest(String title, boolean isPublic, Long folderId) {
}
