package com.softech.entrenaback.assigned.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class AssignedBlockDto {

    private String id;

    @JsonProperty("_combined")
    private Boolean combined;

    private Integer series;
    private Integer reps;

    @JsonProperty("rest_time")
    private String restTime;

    private String indications;

    @JsonProperty("sub_exercises")
    private List<Map<String, Object>> subExercises;

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

    public List<Map<String, Object>> getSubExercises() { return subExercises; }
    public void setSubExercises(List<Map<String, Object>> subExercises) { this.subExercises = subExercises; }
}
