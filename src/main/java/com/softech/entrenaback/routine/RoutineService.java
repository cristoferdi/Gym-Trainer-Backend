package com.softech.entrenaback.routine;

import com.softech.entrenaback.config.ResourceNotFoundException;
import com.softech.entrenaback.routine.dto.*;
import com.softech.entrenaback.trainer.TrainerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final TrainerRepository trainerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public RoutineService(RoutineRepository routineRepository, TrainerRepository trainerRepository) {
        this.routineRepository = routineRepository;
        this.trainerRepository = trainerRepository;
    }

    @Transactional
    public RoutineResponse create(String trainerEmail, CreateRoutineRequest request) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var routine = new Routine();
        routine.setTrainer(trainer);
        routine.setName(request.getName());
        routine.setDescription(request.getDescription());
        routine.setCoachName(trainer.getFullName());

        for (var dayDto : request.getDays()) {
            var day = new RoutineDay();
            day.setRoutine(routine);
            day.setName(dayDto.getDayName());
            day.setOrderIndex(dayDto.getOrderIndex());
            routine.getDays().add(day);

            if (dayDto.getBlocks() != null) {
                for (var blockDto : dayDto.getBlocks()) {
                    var block = new ExerciseBlock();
                    block.setDay(day);
                    block.setIsCombined(blockDto.getCombined() != null ? blockDto.getCombined() : false);
                    block.setSeries(blockDto.getSeries());
                    block.setReps(blockDto.getReps());
                    block.setRestTime(blockDto.getRestTime());
                    block.setIndications(blockDto.getIndications());
                    day.getBlocks().add(block);

                    if (blockDto.getSubExercises() != null) {
                        for (int i = 0; i < blockDto.getSubExercises().size(); i++) {
                            var subDto = blockDto.getSubExercises().get(i);
                            var sub = new SubExerciseDetail();
                            sub.setBlock(block);
                            sub.setExerciseId(subDto.getExerciseId());
                            sub.setName(subDto.getName());
                            sub.setGifUrl(subDto.getGifUrl());
                            sub.setVideoUrl(subDto.getVideoUrl());
                            sub.setInstructions(subDto.getInstructions());
                            sub.setOrderIndex(i);
                            block.getSubExercises().add(sub);
                        }
                    }
                }
            }
        }

        routine = routineRepository.save(routine);
        return toResponse(routine);
    }

    public List<RoutineResponse> list(String trainerEmail) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        return routineRepository.findByTrainerId(trainer.getId())
                .stream()
                .map(r -> new RoutineResponse(r.getId(), r.getName(), r.getDescription(),
                        r.getCoachName(), null, r.getCreatedAt(), r.getUpdatedAt()))
                .toList();
    }

    public RoutineResponse getById(String trainerEmail, String routineId) {
        var routine = findOwned(trainerEmail, routineId);
        return toResponse(routine);
    }

    @Transactional
    public RoutineResponse update(String trainerEmail, String routineId, CreateRoutineRequest request) {
        var routine = findOwned(trainerEmail, routineId);

        routine.setName(request.getName());
        routine.setDescription(request.getDescription());

        for (var day : new java.util.ArrayList<>(routine.getDays())) {
            entityManager.remove(entityManager.contains(day) ? day : entityManager.merge(day));
        }
        routine.getDays().clear();
        routineRepository.saveAndFlush(routine);

        for (var dayDto : request.getDays()) {
            var day = new RoutineDay();
            day.setRoutine(routine);
            day.setName(dayDto.getDayName());
            day.setOrderIndex(dayDto.getOrderIndex());
            routine.getDays().add(day);

            if (dayDto.getBlocks() != null) {
                for (var blockDto : dayDto.getBlocks()) {
                    var block = new ExerciseBlock();
                    block.setDay(day);
                    block.setIsCombined(blockDto.getCombined() != null ? blockDto.getCombined() : false);
                    block.setSeries(blockDto.getSeries());
                    block.setReps(blockDto.getReps());
                    block.setRestTime(blockDto.getRestTime());
                    block.setIndications(blockDto.getIndications());
                    day.getBlocks().add(block);

                    if (blockDto.getSubExercises() != null) {
                        for (int i = 0; i < blockDto.getSubExercises().size(); i++) {
                            var subDto = blockDto.getSubExercises().get(i);
                            var sub = new SubExerciseDetail();
                            sub.setBlock(block);
                            sub.setExerciseId(subDto.getExerciseId());
                            sub.setName(subDto.getName());
                            sub.setGifUrl(subDto.getGifUrl());
                            sub.setVideoUrl(subDto.getVideoUrl());
                            sub.setInstructions(subDto.getInstructions());
                            sub.setOrderIndex(i);
                            block.getSubExercises().add(sub);
                        }
                    }
                }
            }
        }

        routine = routineRepository.save(routine);
        return toResponse(routine);
    }

    @Transactional
    public void delete(String trainerEmail, String routineId) {
        var routine = findOwned(trainerEmail, routineId);
        routineRepository.delete(routine);
    }

    private Routine findOwned(String trainerEmail, String routineId) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));

        if (!routine.getTrainer().getId().equals(trainer.getId())) {
            throw new ResourceNotFoundException("Rutina no pertenece a este entrenador");
        }

        return routine;
    }

    private RoutineResponse toResponse(Routine routine) {
        var days = routine.getDays().stream().map(day -> {
            var dayDto = new RoutineDayDto();
            dayDto.setDayId(day.getId());
            dayDto.setDayName(day.getName());
            dayDto.setOrderIndex(day.getOrderIndex());

            var blocks = day.getBlocks().stream().map(block -> {
                var blockDto = new ExerciseBlockDto();
                blockDto.setId(block.getId());
                blockDto.setCombined(block.getIsCombined());
                blockDto.setSeries(block.getSeries());
                blockDto.setReps(block.getReps());
                blockDto.setRestTime(block.getRestTime());
                blockDto.setIndications(block.getIndications());

                var subs = block.getSubExercises().stream().map(sub -> {
                    var subDto = new SubExerciseDto();
                    subDto.setExerciseId(sub.getExerciseId());
                    subDto.setName(sub.getName());
                    subDto.setGifUrl(sub.getGifUrl());
                    subDto.setVideoUrl(sub.getVideoUrl());
                    subDto.setInstructions(sub.getInstructions());
                    return subDto;
                }).toList();

                blockDto.setSubExercises(subs);
                return blockDto;
            }).toList();

            dayDto.setBlocks(blocks);
            return dayDto;
        }).toList();

        return new RoutineResponse(
            routine.getId(), routine.getName(), routine.getDescription(),
            routine.getCoachName(), days, routine.getCreatedAt(), routine.getUpdatedAt()
        );
    }
}
