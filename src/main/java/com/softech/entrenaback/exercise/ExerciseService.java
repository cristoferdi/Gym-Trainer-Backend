package com.softech.entrenaback.exercise;

import com.softech.entrenaback.exercise.dto.ExerciseListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    public ExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    public ExerciseListResponse list(String q, String muscle, String equipment, int page, int limit) {
        Page<Exercise> result = exerciseRepository.search(
            q, muscle, equipment,
            PageRequest.of(page - 1, limit, Sort.by("name").ascending())
        );

        return new ExerciseListResponse(
            result.getContent(),
            result.getTotalElements(),
            result.getTotalPages(),
            page
        );
    }

    public Exercise findById(String id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ejercicio no encontrado: " + id));
    }

    public List<String> getMuscles() {
        return exerciseRepository.findDistinctTargetMuscles();
    }

    public List<String> getEquipment() {
        return exerciseRepository.findDistinctEquipments();
    }
}
