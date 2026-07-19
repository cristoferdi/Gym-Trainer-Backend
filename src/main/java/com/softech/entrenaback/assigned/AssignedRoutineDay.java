package com.softech.entrenaback.assigned;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assigned_routine_days")
public class AssignedRoutineDay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_routine_id", nullable = false)
    private AssignedRoutine assignedRoutine;

    @Column(name = "day_id", nullable = false)
    private String dayId;

    @Column(nullable = false)
    private String name;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @OneToMany(mappedBy = "assignedDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignedBlock> blocks = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AssignedRoutineDay() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public AssignedRoutine getAssignedRoutine() { return assignedRoutine; }
    public void setAssignedRoutine(AssignedRoutine assignedRoutine) { this.assignedRoutine = assignedRoutine; }

    public String getDayId() { return dayId; }
    public void setDayId(String dayId) { this.dayId = dayId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public List<AssignedBlock> getBlocks() { return blocks; }
    public void setBlocks(List<AssignedBlock> blocks) { this.blocks = blocks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
