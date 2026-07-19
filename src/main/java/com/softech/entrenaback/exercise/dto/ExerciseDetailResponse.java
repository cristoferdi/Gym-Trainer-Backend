package com.softech.entrenaback.exercise.dto;

import com.softech.entrenaback.exercise.Exercise;

public class ExerciseDetailResponse {

    private boolean success;
    private Exercise data;

    public ExerciseDetailResponse(Exercise data) {
        this.success = true;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public Exercise getData() { return data; }
}
