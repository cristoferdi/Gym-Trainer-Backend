package com.softech.entrenaback.customexercise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CustomExerciseRequest {

    @NotBlank
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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMuscle() { return muscle; }
    public void setMuscle(String muscle) { this.muscle = muscle; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public List<String> getSecondaryMuscles() { return secondaryMuscles; }
    public void setSecondaryMuscles(List<String> secondaryMuscles) { this.secondaryMuscles = secondaryMuscles; }

    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }
}
