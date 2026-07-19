package com.softech.entrenaback.assigned;

import com.softech.entrenaback.assigned.dto.AssignRoutineRequest;
import com.softech.entrenaback.assigned.dto.AssignedRoutineResponse;
import com.softech.entrenaback.assigned.dto.ShareResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assigned-routines")
public class AssignedRoutineController {

    private final AssignedRoutineService assignedRoutineService;

    public AssignedRoutineController(AssignedRoutineService assignedRoutineService) {
        this.assignedRoutineService = assignedRoutineService;
    }

    @PostMapping
    public ResponseEntity<AssignedRoutineResponse> assign(Authentication auth,
                                                           @Valid @RequestBody AssignRoutineRequest request) {
        var response = assignedRoutineService.assign(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AssignedRoutineResponse>> list(Authentication auth,
                                                               @RequestParam(required = false) String studentId) {
        return ResponseEntity.ok(assignedRoutineService.list(auth.getName(), studentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignedRoutineResponse> getById(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(assignedRoutineService.getById(auth.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignedRoutineResponse> update(Authentication auth, @PathVariable String id,
                                                           @Valid @RequestBody AssignRoutineRequest request) {
        return ResponseEntity.ok(assignedRoutineService.update(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable String id) {
        assignedRoutineService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ShareResponse> share(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(assignedRoutineService.generateShareUrl(auth.getName(), id));
    }

    @GetMapping("/shared/{shareUrl}")
    public ResponseEntity<AssignedRoutineResponse> getByShareUrl(@PathVariable String shareUrl) {
        return ResponseEntity.ok(assignedRoutineService.getByShareUrl(shareUrl));
    }
}
