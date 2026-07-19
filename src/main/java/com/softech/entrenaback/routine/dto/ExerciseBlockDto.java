package com.softech.entrenaback.routine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ExerciseBlockDto {

    private String id;

    @JsonProperty("_combined")
    private Boolean combined;

    private Integer series;

    private Integer reps;

    @JsonProperty("rest_time")
    private String restTime;

    private String indications;

    @JsonProperty("sub_exercises")
    private List<SubExerciseDto> subExercises;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Boolean getCombined() { return combined; }
    public void setCombined(Boolean combined) { this.combined = combined; }

    public Integer getSeries() { return series; }
    public void setSeries(Integer series) { this.series = series; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public String getRestTime() { return restTime; }
    public void setRestTime(String restTime) { this.restTime = restTime; }

    public String getIndications() { return indications; }
    public void setIndications(String indications) { this.indications = indications; }

    public List<SubExerciseDto> getSubExercises() { return subExercises; }
    public void setSubExercises(List<SubExerciseDto> subExercises) { this.subExercises = subExercises; }
}
