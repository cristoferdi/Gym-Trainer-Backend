package com.softech.entrenaback.assigned.dto;

import java.util.List;
import java.util.Map;

public class AssignedBlockDto {

    private String id;
    private Boolean isCombined;
    private Integer series;
    private Integer reps;
    private String restTime;
    private String indications;
    private List<Map<String, Object>> subExercises;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Boolean getIsCombined() { return isCombined; }
    public void setIsCombined(Boolean isCombined) { this.isCombined = isCombined; }

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
