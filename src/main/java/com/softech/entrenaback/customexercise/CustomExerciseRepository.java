package com.softech.entrenaback.customexercise;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomExerciseRepository extends JpaRepository<CustomExercise, String> {
    List<CustomExercise> findByTrainerId(String trainerId);
    List<CustomExercise> findByTrainerIdAndUpdatedAtAfter(String trainerId, LocalDateTime since);

    @Query("SELECT c.originalGlobalId FROM CustomExercise c WHERE c.trainer.id = :trainerId AND c.originalGlobalId IS NOT NULL")
    List<String> findShadowedGlobalIds(@Param("trainerId") String trainerId);
}
