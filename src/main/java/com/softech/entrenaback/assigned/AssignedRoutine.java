package com.softech.entrenaback.assigned;

import com.softech.entrenaback.student.Student;
import com.softech.entrenaback.trainer.Trainer;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assigned_routines")
public class AssignedRoutine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_goal")
    private String studentGoal;

    @Column(name = "coach_name")
    private String coachName;

    @Column(name = "share_url", unique = true)
    private String shareUrl;

    @OneToMany(mappedBy = "assignedRoutine", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<AssignedRoutineDay> days = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AssignedRoutine() {}

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

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentGoal() { return studentGoal; }
    public void setStudentGoal(String studentGoal) { this.studentGoal = studentGoal; }

    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }

    public String getShareUrl() { return shareUrl; }
    public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }

    public List<AssignedRoutineDay> getDays() { return days; }
    public void setDays(List<AssignedRoutineDay> days) { this.days = days; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
