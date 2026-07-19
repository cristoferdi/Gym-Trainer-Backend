package com.softech.entrenaback.ai;

import com.softech.entrenaback.ai.dto.AiGenerateRequest;
import com.softech.entrenaback.ai.dto.AiGenerateResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;
    private final RateLimiterService rateLimiterService;

    public AiController(AiService aiService, RateLimiterService rateLimiterService) {
        this.aiService = aiService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/generate-exercise")
    public ResponseEntity<?> generate(Authentication auth,
                                       @Valid @RequestBody AiGenerateRequest request) {
        if (!rateLimiterService.isAllowed(auth.getName())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "too_many_requests",
                            "message", "Demasiadas solicitudes. Intenta en 1 minuto.",
                            "statusCode", 429));
        }

        var response = aiService.generate(request.exerciseName());
        return ResponseEntity.ok(response);
    }
}
