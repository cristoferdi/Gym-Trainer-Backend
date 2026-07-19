package com.softech.entrenaback.routine;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routine_days")
public class RoutineDay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Column(nullable = false)
    private String name;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ExerciseBlock> blocks = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RoutineDay() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Routine getRoutine() { return routine; }
    public void setRoutine(Routine routine) { this.routine = routine; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public List<ExerciseBlock> getBlocks() { return blocks; }
    public void setBlocks(List<ExerciseBlock> blocks) { this.blocks = blocks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
