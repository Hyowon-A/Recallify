package com.andy.recallify.mcq;

import java.util.List;

public record McqDto(Long id, String question, String explanation, List<OptionDto> options) {}

