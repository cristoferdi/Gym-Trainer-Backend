package com.softech.entrenaback.sync;

import com.softech.entrenaback.sync.dto.SyncPushRequest;
import com.softech.entrenaback.sync.dto.SyncPullResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/push")
    public ResponseEntity<Map<String, Object>> push(Authentication auth,
                                                     @RequestBody SyncPushRequest request) {
        var result = syncService.push(auth.getName(), request.getOperations());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/pull")
    public ResponseEntity<SyncPullResponse> pull(Authentication auth,
                                                  @RequestBody Map<String, String> body) {
        var since = body.containsKey("lastSyncTimestamp")
                ? LocalDateTime.parse(body.get("lastSyncTimestamp"))
                : LocalDateTime.MIN;

        var response = syncService.pull(auth.getName(), since);
        return ResponseEntity.ok(response);
    }
}
