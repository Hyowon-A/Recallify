package com.andy.recallify.set.dto;

import java.util.List;

public record EditSetRequest(Long setId, String title, String type, Boolean isPublic, List<Long> deletedIds) {
}
