package com.softech.entrenaback.routine;

import com.softech.entrenaback.routine.dto.CreateRoutineRequest;
import com.softech.entrenaback.routine.dto.RoutineResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routines")
public class RoutineController {

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    @PostMapping
    public ResponseEntity<RoutineResponse> create(Authentication auth,
                                                   @Valid @RequestBody CreateRoutineRequest request) {
        var response = routineService.create(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RoutineResponse>> list(Authentication auth) {
        return ResponseEntity.ok(routineService.list(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutineResponse> getById(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(routineService.getById(auth.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutineResponse> update(Authentication auth, @PathVariable String id,
                                                   @Valid @RequestBody CreateRoutineRequest request) {
        return ResponseEntity.ok(routineService.update(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable String id) {
        routineService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
