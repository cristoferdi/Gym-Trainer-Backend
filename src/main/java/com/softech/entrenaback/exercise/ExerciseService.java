package com.softech.entrenaback.exercise;

import com.softech.entrenaback.config.ResourceNotFoundException;
import com.softech.entrenaback.customexercise.CustomExerciseRepository;
import com.softech.entrenaback.exercise.dto.ExerciseListResponse;
import com.softech.entrenaback.exercise.dto.UnifiedExerciseResponse;
import com.softech.entrenaback.trainer.TrainerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final CustomExerciseRepository customExerciseRepository;
    private final TrainerRepository trainerRepository;

    public ExerciseService(ExerciseRepository exerciseRepository,
                           CustomExerciseRepository customExerciseRepository,
                           TrainerRepository trainerRepository) {
        this.exerciseRepository = exerciseRepository;
        this.customExerciseRepository = customExerciseRepository;
        this.trainerRepository = trainerRepository;
    }

    public ExerciseListResponse list(String trainerEmail, String q, String muscle, String equipment, int page, int limit) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var excludedIds = customExerciseRepository.findShadowedGlobalIds(trainer.getId());
        if (excludedIds.isEmpty()) {
            excludedIds = List.of("DUMMY_ID_TO_PREVENT_SQL_ERROR");
        }

        Page<Exercise> result = exerciseRepository.search(
            q, muscle, equipment, excludedIds,
            PageRequest.of(page - 1, limit, Sort.by("name").ascending())
        );

        var unified = result.getContent().stream()
                .map(UnifiedExerciseResponse::fromGlobal)
                .collect(Collectors.toList());

        return new ExerciseListResponse(
            unified,
            result.getTotalElements(),
            result.getTotalPages(),
            page
        );
    }

    public Exercise findById(String id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado: " + id));
    }

    public List<String> getMuscles() {
        return exerciseRepository.findDistinctTargetMuscles();
    }

    public List<String> getEquipment() {
        return exerciseRepository.findDistinctEquipments();
    }
}
