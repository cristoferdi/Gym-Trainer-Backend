package com.softech.entrenaback.customexercise;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softech.entrenaback.customexercise.dto.CustomExerciseRequest;
import com.softech.entrenaback.customexercise.dto.CustomExerciseResponse;
import com.softech.entrenaback.trainer.TrainerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomExerciseService {

    private final CustomExerciseRepository customExerciseRepository;
    private final TrainerRepository trainerRepository;
    private final ObjectMapper objectMapper;

    public CustomExerciseService(CustomExerciseRepository customExerciseRepository,
                                  TrainerRepository trainerRepository,
                                  ObjectMapper objectMapper) {
        this.customExerciseRepository = customExerciseRepository;
        this.trainerRepository = trainerRepository;
        this.objectMapper = objectMapper;
    }

    public CustomExerciseResponse create(String trainerEmail, CustomExerciseRequest request) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var ce = new CustomExercise();
        ce.setTrainer(trainer);
        applyRequest(ce, request);

        return CustomExerciseResponse.from(customExerciseRepository.save(ce));
    }

    public List<CustomExerciseResponse> list(String trainerEmail) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        return customExerciseRepository.findByTrainerId(trainer.getId())
                .stream()
                .map(CustomExerciseResponse::from)
                .toList();
    }

    public CustomExerciseResponse getById(String trainerEmail, String id) {
        var ce = findOwned(trainerEmail, id);
        return CustomExerciseResponse.from(ce);
    }

    public CustomExerciseResponse update(String trainerEmail, String id, CustomExerciseRequest request) {
        var ce = findOwned(trainerEmail, id);
        applyRequest(ce, request);
        return CustomExerciseResponse.from(customExerciseRepository.save(ce));
    }

    public void delete(String trainerEmail, String id) {
        var ce = findOwned(trainerEmail, id);
        customExerciseRepository.delete(ce);
    }

    private CustomExercise findOwned(String trainerEmail, String id) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var ce = customExerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ejercicio personalizado no encontrado"));

        if (!ce.getTrainer().getId().equals(trainer.getId())) {
            throw new IllegalArgumentException("Ejercicio no pertenece a este entrenador");
        }

        return ce;
    }

    private void applyRequest(CustomExercise ce, CustomExerciseRequest r) {
        ce.setName(r.getName());
        ce.setMuscle(r.getMuscle() != null ? r.getMuscle() : "");
        ce.setEquipment(r.getEquipment() != null ? r.getEquipment() : "");
        ce.setGifUrl(r.getGifUrl() != null ? r.getGifUrl() : "");
        ce.setVideoUrl(r.getVideoUrl() != null ? r.getVideoUrl() : "");
        ce.setTarget(r.getTarget() != null ? r.getTarget() : "");
        ce.setSecondaryMuscles(toJson(r.getSecondaryMuscles()));
        ce.setInstructions(r.getInstructions());
    }

    private String toJson(List<String> list) {
        try {
            return list != null ? objectMapper.writeValueAsString(list) : "[]";
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
