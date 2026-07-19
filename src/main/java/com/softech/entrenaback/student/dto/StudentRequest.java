package com.softech.entrenaback.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentRequest(
    @NotBlank String fullName,
    String phoneNumber,
    @NotNull Integer age,
    String gender,
    String disciplina,
    String objetivos
) {}
