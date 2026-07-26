package com.softech.entrenaback.customexercise;

import com.softech.entrenaback.trainer.Trainer;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "custom_exercises")
public class CustomExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String muscle;

    @Column(nullable = false)
    private String equipment;

    @Column(name = "gif_url", nullable = false)
    private String gifUrl;

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    private String target;

    @Column(name = "secondary_muscles", nullable = false)
    private String secondaryMuscles;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> instructions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CustomExercise() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Trainer getTrainer() { return trainer; }
    public void setTrainer(Trainer trainer) { this.trainer = trainer; }

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

    public String getSecondaryMuscles() { return secondaryMuscles; }
    public void setSecondaryMuscles(String secondaryMuscles) { this.secondaryMuscles = secondaryMuscles; }

    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
