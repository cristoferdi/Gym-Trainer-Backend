package com.softech.entrenaback.routine;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercise_blocks")
public class ExerciseBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private RoutineDay day;

    @Column(name = "is_combined", nullable = false)
    private Boolean isCombined = false;

    @Column(nullable = false)
    private Integer series;

    @Column(nullable = false)
    private Integer reps;

    @Column(name = "rest_time")
    private String restTime;

    @Column(columnDefinition = "jsonb")
    private String indications;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<SubExerciseDetail> subExercises = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ExerciseBlock() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public RoutineDay getDay() { return day; }
    public void setDay(RoutineDay day) { this.day = day; }

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

    public List<SubExerciseDetail> getSubExercises() { return subExercises; }
    public void setSubExercises(List<SubExerciseDetail> subExercises) { this.subExercises = subExercises; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
