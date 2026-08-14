package com.andy.recallify.features.set.dto;

public record FolderDto(Long id, String publicId, String title, boolean isPublic, int mcqSetCount, int flashSetCount) {
}
