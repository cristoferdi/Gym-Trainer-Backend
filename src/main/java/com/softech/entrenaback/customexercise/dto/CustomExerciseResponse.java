package com.softech.entrenaback.customexercise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softech.entrenaback.customexercise.CustomExercise;
import java.util.Arrays;
import java.util.List;

public class CustomExerciseResponse {

    private String id;
    private String name;
    private String muscle;
    private String equipment;

    @JsonProperty("gifUrl")
    private String gifUrl;

    @JsonProperty("videoUrl")
    private String videoUrl;

    private String target;

    @JsonProperty("secondaryMuscles")
    private List<String> secondaryMuscles;

    private List<String> instructions;

    public CustomExerciseResponse() {}

    public CustomExerciseResponse(String id, String name, String muscle, String equipment,
                                  String gifUrl, String videoUrl, String target,
                                  List<String> secondaryMuscles, List<String> instructions) {
        this.id = id;
        this.name = name;
        this.muscle = muscle;
        this.equipment = equipment;
        this.gifUrl = gifUrl;
        this.videoUrl = videoUrl;
        this.target = target;
        this.secondaryMuscles = secondaryMuscles;
        this.instructions = instructions;
    }

    public static CustomExerciseResponse from(CustomExercise ce) {
        return new CustomExerciseResponse(
            ce.getId(), ce.getName(), ce.getMuscle(), ce.getEquipment(),
            ce.getGifUrl(), ce.getVideoUrl(), ce.getTarget(),
            parseJsonList(ce.getSecondaryMuscles()),
            parseJsonList(ce.getInstructions())
        );
    }

    private static List<String> parseJsonList(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return List.of();
        var s = json.replaceAll("[\\[\\]\"]", "");
        return s.isEmpty() ? List.of() : Arrays.asList(s.split("\\s*,\\s*"));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMuscle() { return muscle; }
    public String getEquipment() { return equipment; }
    public String getGifUrl() { return gifUrl; }
    public String getVideoUrl() { return videoUrl; }
    public String getTarget() { return target; }
    public List<String> getSecondaryMuscles() { return secondaryMuscles; }
    public List<String> getInstructions() { return instructions; }
}
