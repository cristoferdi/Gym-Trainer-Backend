package com.softech.entrenaback.exercise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softech.entrenaback.customexercise.CustomExercise;
import com.softech.entrenaback.exercise.Exercise;

import java.util.List;

public class UnifiedExerciseResponse {

    private String id;
    private String name;
    private String muscle;
    private String equipment;
    private String gifUrl;
    private String videoUrl;
    private List<String> instructions;
    private boolean isCustom;

    public UnifiedExerciseResponse() {}

    public UnifiedExerciseResponse(String id, String name, String muscle, String equipment,
                                   String gifUrl, String videoUrl, List<String> instructions, boolean isCustom) {
        this.id = id;
        this.name = name;
        this.muscle = muscle;
        this.equipment = equipment;
        this.gifUrl = gifUrl;
        this.videoUrl = videoUrl;
        this.instructions = instructions;
        this.isCustom = isCustom;
    }

    public static UnifiedExerciseResponse fromGlobal(Exercise exercise) {
        return new UnifiedExerciseResponse(
            exercise.getId(),
            exercise.getName(),
            extractFirst(exercise.getTargetMuscles()),
            extractFirst(exercise.getEquipments()),
            exercise.getGifUrl(),
            exercise.getVideoUrl(),
            exercise.getInstructions(),
            false
        );
    }

    public static UnifiedExerciseResponse fromCustom(CustomExercise custom) {
        return new UnifiedExerciseResponse(
            custom.getId(),
            custom.getName(),
            custom.getMuscle(),
            custom.getEquipment(),
            custom.getGifUrl(),
            custom.getVideoUrl(),
            custom.getInstructions(),
            true
        );
    }

    private static String extractFirst(String commaSeparated) {
        if (commaSeparated == null || commaSeparated.isBlank()) {
            return "";
        }
        return commaSeparated.split(",")[0].trim();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMuscle() { return muscle; }
    public String getEquipment() { return equipment; }
    public String getGifUrl() { return gifUrl; }
    public String getVideoUrl() { return videoUrl; }
    public List<String> getInstructions() { return instructions; }

    @JsonProperty("isCustom")
    public boolean isCustom() { return isCustom; }
}
