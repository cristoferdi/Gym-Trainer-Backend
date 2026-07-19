package com.softech.entrenaback.ai;

import com.softech.entrenaback.ai.dto.AiGenerateResponse;
import com.softech.entrenaback.ai.dto.ExerciseInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;

    public AiService(RestTemplate restTemplate,
                     @Value("${gemini.api-key}") String apiKey,
                     @Value("${gemini.api-url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public AiGenerateResponse generate(String exerciseName) {
        var requestBody = buildGeminiRequest(exerciseName);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var entity = new HttpEntity<>(requestBody, headers);
        var response = restTemplate.postForEntity(apiUrl + "?key=" + apiKey, entity, Map.class);

        if (response.getBody() == null) {
            throw new RuntimeException("Sin respuesta de Gemini");
        }

        var text = extractText(response.getBody());
        var exercise = parseExercise(text);
        return new AiGenerateResponse(exercise);
    }

    private Map<String, Object> buildGeminiRequest(String exerciseName) {
        var prompt = """
            Genera información estructurada de un ejercicio de gimnasio llamado "%s".
            Responde SOLO con JSON válido sin markdown, con esta estructura exacta:
            {
              "name": "Nombre del ejercicio",
              "muscle": "Músculo principal",
              "equipment": "Equipamiento necesario",
              "target": "Objetivo del ejercicio",
              "secondaryMuscles": ["musculo1", "musculo2"],
              "instructions": ["Paso 1: ...", "Paso 2: ..."]
            }
            """.formatted(exerciseName);

        var part = Map.of("text", prompt);
        var content = Map.of("parts", List.of(part));
        return Map.of("contents", List.of(content));
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> body) {
        var candidates = (List<Map<String, Object>>) body.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Sin candidatos en respuesta Gemini");
        }
        var content = (Map<String, Object>) candidates.get(0).get("content");
        var parts = (List<Map<String, Object>>) content.get("parts");
        return (String) parts.get(0).get("text");
    }

    @SuppressWarnings("unchecked")
    private ExerciseInfo parseExercise(String text) {
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var root = mapper.readValue(text, Map.class);

            var info = new ExerciseInfo();
            info.setId(UUID.randomUUID().toString());
            info.setName((String) root.getOrDefault("name", ""));
            info.setMuscle((String) root.getOrDefault("muscle", ""));
            info.setEquipment((String) root.getOrDefault("equipment", ""));
            info.setGifUrl("");
            info.setVideoUrl("");
            info.setTarget((String) root.getOrDefault("target", ""));
            info.setSecondaryMuscles((List<String>) root.getOrDefault("secondaryMuscles", List.of()));
            info.setInstructions((List<String>) root.getOrDefault("instructions", List.of()));
            info.setCustom(true);
            return info;
        } catch (Exception e) {
            throw new RuntimeException("Error parseando respuesta de Gemini", e);
        }
    }
}
