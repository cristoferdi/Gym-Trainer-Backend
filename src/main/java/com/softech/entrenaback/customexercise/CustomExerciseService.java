package com.softech.entrenaback.customexercise;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softech.entrenaback.config.ResourceNotFoundException;
import com.softech.entrenaback.customexercise.dto.CustomExerciseRequest;
import com.softech.entrenaback.exercise.ExerciseRepository;
import com.softech.entrenaback.exercise.dto.UnifiedExerciseResponse;
import com.softech.entrenaback.trainer.TrainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomExerciseService {

private final CustomExerciseRepository customExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final TrainerRepository trainerRepository;
    private final ObjectMapper objectMapper;

    public CustomExerciseService(CustomExerciseRepository customExerciseRepository,
                                 ExerciseRepository exerciseRepository,
                                 TrainerRepository trainerRepository,
                                 ObjectMapper objectMapper) {
        this.customExerciseRepository = customExerciseRepository;
        this.exerciseRepository = exerciseRepository;
        this.trainerRepository = trainerRepository;
        this.objectMapper = objectMapper;
    }

    public UnifiedExerciseResponse create(String trainerEmail, CustomExerciseRequest request) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var ce = new CustomExercise();
        ce.setTrainer(trainer);
        applyRequest(ce, request);

        return UnifiedExerciseResponse.fromCustom(customExerciseRepository.save(ce));
    }

    public List<UnifiedExerciseResponse> list(String trainerEmail) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        return customExerciseRepository.findByTrainerId(trainer.getId())
                .stream()
                .map(UnifiedExerciseResponse::fromCustom)
                .toList();
    }

    public UnifiedExerciseResponse getById(String trainerEmail, String id) {
        var ce = findOwned(trainerEmail, id);
        return UnifiedExerciseResponse.fromCustom(ce);
    }

    public UnifiedExerciseResponse update(String trainerEmail, String id, CustomExerciseRequest request) {
        var ce = findOwned(trainerEmail, id);
        applyRequest(ce, request);
        return UnifiedExerciseResponse.fromCustom(customExerciseRepository.save(ce));
    }

    public void delete(String trainerEmail, String id) {
        var ce = findOwned(trainerEmail, id);
        customExerciseRepository.delete(ce);
    }

    @Transactional
    public UnifiedExerciseResponse cloneFromGlobal(String trainerEmail, String globalId) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var global = exerciseRepository.findById(globalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio global no encontrado: " + globalId));

        var ce = new CustomExercise();
        ce.setTrainer(trainer);
        ce.setOriginalGlobalId(globalId);
        ce.setName(global.getName());
        ce.setMuscle(global.getTargetMuscles());
        ce.setEquipment(global.getEquipments());
        ce.setGifUrl(global.getGifUrl());
        ce.setVideoUrl(global.getVideoUrl());
        ce.setTarget("");
        ce.setSecondaryMuscles(global.getSecondaryMuscles());
        ce.setInstructions(global.getInstructions());

        return UnifiedExerciseResponse.fromCustom(customExerciseRepository.save(ce));
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
