package com.andy.recallify.features.set.dto;

import java.util.List;

public record FlashcardDto(Long id, String front, String back,
                           float interval_hours, float ef, int repetitions, String srsType) {}

