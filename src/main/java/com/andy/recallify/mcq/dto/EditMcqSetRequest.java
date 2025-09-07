package com.andy.recallify.mcq.dto;

import java.util.List;

public record EditMcqSetRequest(Long setId, String title, Boolean isPublic, List<Long> deletedIds) {
}
