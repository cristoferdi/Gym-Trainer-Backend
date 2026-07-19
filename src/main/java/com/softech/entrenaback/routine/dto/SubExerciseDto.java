package com.softech.entrenaback.routine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SubExerciseDto {

    @JsonProperty("exercise_id")
    private String exerciseId;

    private String name;

    @JsonProperty("gif_url")
    private String gifUrl;

    @JsonProperty("video_url")
    private String videoUrl;

    private List<String> instructions;

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }
}
