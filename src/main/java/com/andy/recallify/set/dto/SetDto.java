package com.andy.recallify.set.dto;

public record SetDto(Long id, String title, boolean isPublic, int count,
                     String type, boolean isOwner, int newC, int learn, int due) {
}
