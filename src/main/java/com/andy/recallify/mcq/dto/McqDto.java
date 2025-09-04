package com.andy.recallify.mcq.dto;

import java.util.List;

public record McqDto(Long id, String question, String explanation, List<OptionDto> options) {}

