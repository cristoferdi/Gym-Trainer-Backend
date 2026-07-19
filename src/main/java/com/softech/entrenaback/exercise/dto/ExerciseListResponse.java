package com.softech.entrenaback.exercise.dto;

import com.softech.entrenaback.exercise.Exercise;
import java.util.List;

public class ExerciseListResponse {

    private boolean success;
    private Metadata metadata;
    private List<Exercise> data;

    public ExerciseListResponse(List<Exercise> data, long totalExercises, int totalPages, int currentPage) {
        this.success = true;
        this.metadata = new Metadata(totalExercises, totalPages, currentPage);
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public Metadata getMetadata() { return metadata; }
    public List<Exercise> getData() { return data; }

    public static class Metadata {
        private long totalExercises;
        private int totalPages;
        private int currentPage;

        public Metadata(long totalExercises, int totalPages, int currentPage) {
            this.totalExercises = totalExercises;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
        }

        public long getTotalExercises() { return totalExercises; }
        public int getTotalPages() { return totalPages; }
        public int getCurrentPage() { return currentPage; }
    }
}
