package com.softech.entrenaback.assigned;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "assigned_blocks")
public class AssignedBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_day_id", nullable = false)
    private AssignedRoutineDay assignedDay;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "block_data", nullable = false, columnDefinition = "jsonb")
    private String blockData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AssignedBlock() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public AssignedRoutineDay getAssignedDay() { return assignedDay; }
    public void setAssignedDay(AssignedRoutineDay assignedDay) { this.assignedDay = assignedDay; }

    public String getBlockData() { return blockData; }
    public void setBlockData(String blockData) { this.blockData = blockData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
