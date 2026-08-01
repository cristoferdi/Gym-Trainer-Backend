package com.softech.entrenaback.exercise;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, String> {

    @Query("SELECT e FROM Exercise e WHERE " +
            "(CAST(:q AS String) IS NULL OR LOWER(e.nameNormalized) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%'))) AND " +
            "(CAST(:muscle AS String) IS NULL OR LOWER(e.targetMuscles) LIKE LOWER(CONCAT('%', CAST(:muscle AS String), '%'))) AND " +
            "(CAST(:equipment AS String) IS NULL OR LOWER(e.equipments) LIKE LOWER(CONCAT('%', CAST(:equipment AS String), '%'))) AND " +
            "(e.id NOT IN :excludedIds)")
    Page<Exercise> search(@Param("q") String q,
                          @Param("muscle") String muscle,
                          @Param("equipment") String equipment,
                          @Param("excludedIds") List<String> excludedIds,
                          Pageable pageable);

    @Query("SELECT DISTINCT e.targetMuscles FROM Exercise e ORDER BY e.targetMuscles")
    List<String> findDistinctTargetMuscles();

    @Query("SELECT DISTINCT e.equipments FROM Exercise e ORDER BY e.equipments")
    List<String> findDistinctEquipments();
}
