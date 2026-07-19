package com.softech.entrenaback.customexercise;

import com.softech.entrenaback.customexercise.dto.CustomExerciseRequest;
import com.softech.entrenaback.customexercise.dto.CustomExerciseResponse;
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
    public ResponseEntity<CustomExerciseResponse> create(Authentication auth,
                                                          @Valid @RequestBody CustomExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customExerciseService.create(auth.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<CustomExerciseResponse>> list(Authentication auth) {
        return ResponseEntity.ok(customExerciseService.list(auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomExerciseResponse> update(Authentication auth, @PathVariable String id,
                                                          @Valid @RequestBody CustomExerciseRequest request) {
        return ResponseEntity.ok(customExerciseService.update(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable String id) {
        customExerciseService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
