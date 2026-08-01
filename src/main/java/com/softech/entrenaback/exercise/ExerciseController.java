package com.softech.entrenaback.exercise;

import com.softech.entrenaback.exercise.dto.ExerciseDetailResponse;
import com.softech.entrenaback.exercise.dto.ExerciseListResponse;
import com.softech.entrenaback.exercise.dto.MuscleEquipmentResponse;
import com.softech.entrenaback.exercise.dto.UnifiedExerciseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public ResponseEntity<ExerciseListResponse> list(Authentication auth,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String muscle,
            @RequestParam(required = false) String equipment,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(exerciseService.list(auth.getName(), q, muscle, equipment, page, limit));
    }

    @GetMapping("/{exerciseId}")
    public ResponseEntity<ExerciseDetailResponse> getById(@PathVariable String exerciseId) {
        var exercise = exerciseService.findById(exerciseId);
        return ResponseEntity.ok(new ExerciseDetailResponse(UnifiedExerciseResponse.fromGlobal(exercise)));
    }

    @GetMapping("/muscles")
    public ResponseEntity<MuscleEquipmentResponse> getMuscles() {
        var items = exerciseService.getMuscles().stream()
                .map(m -> new MuscleEquipmentResponse.Item(cleanKey(m), m))
                .toList();
        return ResponseEntity.ok(new MuscleEquipmentResponse(items));
    }

    @GetMapping("/equipment")
    public ResponseEntity<MuscleEquipmentResponse> getEquipment() {
        var items = exerciseService.getEquipment().stream()
                .map(e -> new MuscleEquipmentResponse.Item(cleanKey(e), e))
                .toList();
        return ResponseEntity.ok(new MuscleEquipmentResponse(items));
    }

    private String cleanKey(String value) {
        return value.toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-záéíóúñ0-9_]", "");
    }
}
