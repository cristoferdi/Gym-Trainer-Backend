package com.softech.entrenaback.ai.dto;

import java.util.List;

public class ExerciseInfo {

    private String id;
    private String name;
    private String muscle;
    private String equipment;
    private String gifUrl;
    private String videoUrl;
    private String target;
    private List<String> secondaryMuscles;
    private List<String> instructions;
    private boolean isCustom;

    public ExerciseInfo() {}

    public ExerciseInfo(String id, String name, String muscle, String equipment, String gifUrl,
                        String videoUrl, String target, List<String> secondaryMuscles,
                        List<String> instructions, boolean isCustom) {
        this.id = id;
        this.name = name;
        this.muscle = muscle;
        this.equipment = equipment;
        this.gifUrl = gifUrl;
        this.videoUrl = videoUrl;
        this.target = target;
        this.secondaryMuscles = secondaryMuscles;
        this.instructions = instructions;
        this.isCustom = isCustom;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }
}
