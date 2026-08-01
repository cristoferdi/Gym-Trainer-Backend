package com.softech.entrenaback.assigned;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softech.entrenaback.assigned.dto.*;
import com.softech.entrenaback.routine.ExerciseBlock;
import com.softech.entrenaback.routine.RoutineRepository;
import com.softech.entrenaback.student.StudentRepository;
import com.softech.entrenaback.trainer.TrainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AssignedRoutineService {

    private final AssignedRoutineRepository assignedRoutineRepository;
    private final RoutineRepository routineRepository;
    private final StudentRepository studentRepository;
    private final TrainerRepository trainerRepository;
    private final ObjectMapper objectMapper;

    public AssignedRoutineService(AssignedRoutineRepository assignedRoutineRepository,
                                  RoutineRepository routineRepository,
                                  StudentRepository studentRepository,
                                  TrainerRepository trainerRepository,
                                  ObjectMapper objectMapper) {
        this.assignedRoutineRepository = assignedRoutineRepository;
        this.routineRepository = routineRepository;
        this.studentRepository = studentRepository;
        this.trainerRepository = trainerRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AssignedRoutineResponse assign(String trainerEmail, AssignRoutineRequest request) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var template = routineRepository.findById(request.getRoutineTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("Plantilla de rutina no encontrada"));

        if (!template.getTrainer().getId().equals(trainer.getId())) {
            throw new IllegalArgumentException("Plantilla no pertenece a este entrenador");
        }

        var student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado"));

        if (!student.getTrainer().getId().equals(trainer.getId())) {
            throw new IllegalArgumentException("Alumno no pertenece a este entrenador");
        }

        var assigned = new AssignedRoutine();
        assigned.setTrainer(trainer);
        assigned.setStudent(student);
        assigned.setName(template.getName());
        assigned.setDescription(template.getDescription());
        assigned.setStudentName(student.getFullName());
        assigned.setStudentGoal(student.getObjetivos());
        assigned.setCoachName(request.getCoachName() != null ? request.getCoachName() : trainer.getFullName());
        assigned.setShareUrl(generateShareUrl());

        for (var day : template.getDays()) {
            var assignedDay = new AssignedRoutineDay();
            assignedDay.setAssignedRoutine(assigned);
            assignedDay.setDayId(day.getId());
            assignedDay.setName(day.getName());
            assignedDay.setOrderIndex(day.getOrderIndex());
            assigned.getDays().add(assignedDay);

            for (var block : day.getBlocks()) {
                var assignedBlock = new AssignedBlock();
                assignedBlock.setAssignedDay(assignedDay);
                assignedBlock.setBlockData(serializeBlock(block));
                assignedDay.getBlocks().add(assignedBlock);
            }
        }

        assigned = assignedRoutineRepository.save(assigned);
        return toResponse(assigned);
    }

    public List<AssignedRoutineResponse> list(String trainerEmail, String studentId) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        List<AssignedRoutine> routines;
        if (studentId != null) {
            var student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado"));
            if (!student.getTrainer().getId().equals(trainer.getId())) {
                throw new IllegalArgumentException("Alumno no pertenece a este entrenador");
            }
            routines = assignedRoutineRepository.findByStudentId(studentId);
        } else {
            routines = assignedRoutineRepository.findByTrainerId(trainer.getId());
        }

        return routines.stream().map(this::toResponse).toList();
    }

    public AssignedRoutineResponse getById(String trainerEmail, String assignedId) {
        var assigned = findOwned(trainerEmail, assignedId);
        return toResponse(assigned);
    }

    @Transactional
    public AssignedRoutineResponse update(String trainerEmail, String assignedId, AssignRoutineRequest request) {
        var assigned = findOwned(trainerEmail, assignedId);

        if (request.getStudentId() != null) {
            var student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado"));
            assigned.setStudent(student);
            assigned.setStudentName(student.getFullName());
            assigned.setStudentGoal(student.getObjetivos());
        }

        if (request.getCoachName() != null) {
            assigned.setCoachName(request.getCoachName());
        }

        assigned = assignedRoutineRepository.save(assigned);
        return toResponse(assigned);
    }

    @Transactional
    public void delete(String trainerEmail, String assignedId) {
        var assigned = findOwned(trainerEmail, assignedId);
        assignedRoutineRepository.delete(assigned);
    }

    @Transactional
    public ShareResponse generateShareUrl(String trainerEmail, String assignedId) {
        var assigned = findOwned(trainerEmail, assignedId);
        assigned.setShareUrl(generateShareUrl());
        assigned = assignedRoutineRepository.save(assigned);
        return new ShareResponse(assigned.getShareUrl());
    }

    public AssignedRoutineResponse getByShareUrl(String shareUrl) {
        var assigned = assignedRoutineRepository.findByShareUrl(shareUrl)
                .orElseThrow(() -> new IllegalArgumentException("Rutina compartida no encontrada"));
        return toResponse(assigned);
    }

    private AssignedRoutine findOwned(String trainerEmail, String assignedId) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var assigned = assignedRoutineRepository.findById(assignedId)
                .orElseThrow(() -> new IllegalArgumentException("Rutina asignada no encontrada"));

        if (!assigned.getTrainer().getId().equals(trainer.getId())) {
            throw new IllegalArgumentException("Rutina asignada no pertenece a este entrenador");
        }

        return assigned;
    }

    private String serializeBlock(ExerciseBlock block) {
        try {
            var map = new LinkedHashMap<String, Object>();
            map.put("id", block.getId());
            map.put("isCombined", block.getIsCombined());
            map.put("series", block.getSeries());
            map.put("reps", block.getReps());
            map.put("restTime", block.getRestTime());
            map.put("indications", block.getIndications());

            var subs = block.getSubExercises().stream().map(sub -> {
                var subMap = new LinkedHashMap<String, Object>();
                subMap.put("exerciseId", sub.getExerciseId());
                subMap.put("name", sub.getName());
                subMap.put("gifUrl", sub.getGifUrl());
                subMap.put("videoUrl", sub.getVideoUrl());
                subMap.put("instructions", sub.getInstructions());
                return subMap;
            }).toList();

            map.put("subExercises", subs);
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando bloque", e);
        }
    }

    private String generateShareUrl() {
        return UUID.randomUUID().toString();
    }

    @SuppressWarnings("unchecked")
    private AssignedRoutineResponse toResponse(AssignedRoutine assigned) {
        var days = assigned.getDays().stream().map(day -> {
            var dayDto = new AssignedDayDto();
            dayDto.setDayId(day.getDayId());
            dayDto.setDayName(day.getName());
            dayDto.setOrderIndex(day.getOrderIndex());

            var blocks = day.getBlocks().stream().map(block -> {
                var blockDto = new AssignedBlockDto();
                blockDto.setId(block.getId());

                try {
                    var data = objectMapper.readValue(block.getBlockData(), Map.class);
                    blockDto.setIsCombined((Boolean) data.get("isCombined"));
                    blockDto.setSeries((Integer) data.get("series"));
                    blockDto.setReps((Integer) data.get("reps"));
                    blockDto.setRestTime((String) data.get("restTime"));
                    blockDto.setIndications((String) data.get("indications"));
                    blockDto.setSubExercises((List<Map<String, Object>>) data.get("subExercises"));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error deserializando bloque", e);
                }

                return blockDto;
            }).toList();

            dayDto.setBlocks(blocks);
            return dayDto;
        }).toList();

        return new AssignedRoutineResponse(
            assigned.getId(), assigned.getName(), assigned.getDescription(),
            assigned.getStudent().getId(), assigned.getStudentName(), assigned.getStudentGoal(),
            assigned.getCoachName(), assigned.getShareUrl(), days, assigned.getCreatedAt()
        );
    }
}
