package com.softech.entrenaback.assigned;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignedRoutineRepository extends JpaRepository<AssignedRoutine, String> {
    List<AssignedRoutine> findByTrainerId(String trainerId);
    List<AssignedRoutine> findByStudentId(String studentId);
    Optional<AssignedRoutine> findByShareUrl(String shareUrl);
    List<AssignedRoutine> findByTrainerIdAndUpdatedAtAfter(String trainerId, LocalDateTime since);
}
