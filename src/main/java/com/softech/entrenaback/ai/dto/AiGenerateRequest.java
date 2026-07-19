package com.softech.entrenaback.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AiGenerateRequest(
    @NotBlank String exerciseName
) {}
