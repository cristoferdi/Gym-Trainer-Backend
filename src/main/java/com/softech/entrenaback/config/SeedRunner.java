package com.softech.entrenaback.config;

import com.softech.entrenaback.exercise.Exercise;
import com.softech.entrenaback.exercise.ExerciseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;

@Component
public class SeedRunner implements CommandLineRunner {

    private final ExerciseRepository exerciseRepository;

    public SeedRunner(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (exerciseRepository.count() > 0) {
            return;
        }

        var resource = new ClassPathResource("exercises_es_v3.json");
        try (InputStream is = resource.getInputStream()) {
            var json = new String(is.readAllBytes());
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var root = mapper.readValue(json, Map.class);
            var data = (List<Map<String, Object>>) root.get("data");

            for (var item : data) {
                var exercise = new Exercise();
                exercise.setId((String) item.get("exerciseId"));
                exercise.setName((String) item.get("name"));
                exercise.setNameNormalized(normalize((String) item.get("name")));
                exercise.setGifUrl((String) item.get("gifUrl"));
                exercise.setVideoUrl("");
                exercise.setTargetMuscles(joinList(item.get("targetMuscles")));
                exercise.setBodyParts(joinList(item.get("bodyParts")));
                exercise.setEquipments(joinList(item.get("equipments")));
                exercise.setSecondaryMuscles(joinList(item.get("secondaryMuscles")));
                //noinspection unchecked
                exercise.setInstructions((List<String>) item.get("instructions"));
                exerciseRepository.save(exercise);
            }
        }
    }

    private String joinList(Object value) {
        if (value instanceof List<?> list) {
            return String.join(",", list.stream().map(Object::toString).toList());
        }
        return "";
    }

    private String joinListPipe(Object value) {
        if (value instanceof List<?> list) {
            return String.join("|", list.stream().map(Object::toString).toList());
        }
        return "";
    }

    private String normalize(String text) {
        if (text == null) return "";
        var normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase().trim();
    }
}
