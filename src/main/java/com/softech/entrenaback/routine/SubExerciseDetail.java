package com.softech.entrenaback.routine;

import jakarta.persistence.*;

@Entity
@Table(name = "sub_exercise_details")
public class SubExerciseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private ExerciseBlock block;

    @Column(name = "exercise_id", nullable = false)
    private String exerciseId;

    @Column(nullable = false)
    private String name;

    @Column(name = "gif_url", nullable = false)
    private String gifUrl;

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    private String muscle;

    @Column(nullable = false)
    private String equipment;

    @Column(columnDefinition = "jsonb")
    private String instructions;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    public SubExerciseDetail() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ExerciseBlock getBlock() { return block; }
    public void setBlock(ExerciseBlock block) { this.block = block; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getMuscle() { return muscle; }
    public void setMuscle(String muscle) { this.muscle = muscle; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
