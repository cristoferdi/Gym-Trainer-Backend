package com.softech.entrenaback.routine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RoutineRepository extends JpaRepository<Routine, String> {
    List<Routine> findByTrainerId(String trainerId);
    List<Routine> findByTrainerIdAndUpdatedAtAfter(String trainerId, LocalDateTime since);
}
