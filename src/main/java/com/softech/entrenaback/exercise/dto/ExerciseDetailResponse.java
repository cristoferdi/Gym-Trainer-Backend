package com.softech.entrenaback.exercise.dto;

public class ExerciseDetailResponse {

    private boolean success;
    private UnifiedExerciseResponse data;

    public ExerciseDetailResponse(UnifiedExerciseResponse data) {
        this.success = true;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public UnifiedExerciseResponse getData() { return data; }
}
