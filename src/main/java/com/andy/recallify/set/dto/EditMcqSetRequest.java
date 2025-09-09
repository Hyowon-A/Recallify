package com.andy.recallify.set.dto;

import java.util.List;

public record EditMcqSetRequest(Long setId, String title, Boolean isPublic, List<Long> deletedIds) {
}
