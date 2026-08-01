package com.softech.entrenaback.customexercise;

import com.softech.entrenaback.customexercise.dto.CustomExerciseRequest;
import com.softech.entrenaback.exercise.dto.UnifiedExerciseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/custom-exercises")
public class CustomExerciseController {

    private final CustomExerciseService customExerciseService;

    public CustomExerciseController(CustomExerciseService customExerciseService) {
        this.customExerciseService = customExerciseService;
    }

    @PostMapping
    public ResponseEntity<UnifiedExerciseResponse> create(Authentication auth,
                                                          @Valid @RequestBody CustomExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customExerciseService.create(auth.getName(), request));
    }

    @PostMapping("/from-global/{globalId}")
    public ResponseEntity<UnifiedExerciseResponse> cloneFromGlobal(Authentication auth,
                                                                  @PathVariable String globalId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customExerciseService.cloneFromGlobal(auth.getName(), globalId));
    }

    @GetMapping
    public ResponseEntity<List<UnifiedExerciseResponse>> list(Authentication auth) {
        return ResponseEntity.ok(customExerciseService.list(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnifiedExerciseResponse> getById(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(customExerciseService.getById(auth.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnifiedExerciseResponse> update(Authentication auth, @PathVariable String id,
                                                        @Valid @RequestBody CustomExerciseRequest request) {
        return ResponseEntity.ok(customExerciseService.update(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable String id) {
        customExerciseService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
