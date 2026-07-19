package com.softech.entrenaback.exercise;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, String> {

    @Query("SELECT e FROM Exercise e WHERE " +
           "(:q IS NULL OR LOWER(e.nameNormalized) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(:muscle IS NULL OR LOWER(e.targetMuscles) LIKE LOWER(CONCAT('%', :muscle, '%'))) AND " +
           "(:equipment IS NULL OR LOWER(e.equipments) LIKE LOWER(CONCAT('%', :equipment, '%')))")
    Page<Exercise> search(@Param("q") String q,
                          @Param("muscle") String muscle,
                          @Param("equipment") String equipment,
                          Pageable pageable);

    @Query("SELECT DISTINCT e.targetMuscles FROM Exercise e ORDER BY e.targetMuscles")
    List<String> findDistinctTargetMuscles();

    @Query("SELECT DISTINCT e.equipments FROM Exercise e ORDER BY e.equipments")
    List<String> findDistinctEquipments();
}
