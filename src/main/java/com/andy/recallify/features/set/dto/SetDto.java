package com.andy.recallify.features.set.dto;

public record SetDto(Long id, String title, boolean isPublic, int count,
                     String type, boolean isOwner, int newC, int learn, int due,
                     Long folderId, String folderTitle) {
    public SetDto(Long id, String title, boolean isPublic, int count,
                  String type, boolean isOwner, int newC, int learn, int due) {
        this(id, title, isPublic, count, type, isOwner, newC, learn, due, null, null);
    }
}
