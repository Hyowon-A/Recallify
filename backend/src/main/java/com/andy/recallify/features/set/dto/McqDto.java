package com.andy.recallify.features.set.dto;

import java.util.List;

public record McqDto(Long id, String question, String explanation, List<OptionDto> options,
                     float interval_hours, float ef, int repetitions, String srsType) {}

