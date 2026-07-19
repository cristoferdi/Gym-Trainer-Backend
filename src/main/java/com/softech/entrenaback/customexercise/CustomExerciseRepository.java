package com.softech.entrenaback.customexercise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomExerciseRepository extends JpaRepository<CustomExercise, String> {
    List<CustomExercise> findByTrainerId(String trainerId);
    List<CustomExercise> findByTrainerIdAndUpdatedAtAfter(String trainerId, LocalDateTime since);
}
