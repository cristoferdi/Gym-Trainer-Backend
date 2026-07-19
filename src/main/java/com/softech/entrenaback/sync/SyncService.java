package com.softech.entrenaback.sync;

import com.softech.entrenaback.assigned.AssignedRoutine;
import com.softech.entrenaback.assigned.AssignedRoutineRepository;
import com.softech.entrenaback.assigned.dto.AssignedRoutineResponse;
import com.softech.entrenaback.customexercise.CustomExercise;
import com.softech.entrenaback.customexercise.CustomExerciseRepository;
import com.softech.entrenaback.customexercise.dto.CustomExerciseResponse;
import com.softech.entrenaback.routine.Routine;
import com.softech.entrenaback.routine.RoutineRepository;
import com.softech.entrenaback.routine.dto.RoutineResponse;
import com.softech.entrenaback.routine.RoutineService;
import com.softech.entrenaback.student.Student;
import com.softech.entrenaback.student.StudentRepository;
import com.softech.entrenaback.sync.dto.SyncOperation;
import com.softech.entrenaback.sync.dto.SyncPullResponse;
import com.softech.entrenaback.trainer.Trainer;
import com.softech.entrenaback.trainer.TrainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SyncService {

    private final TrainerRepository trainerRepository;
    private final StudentRepository studentRepository;
    private final CustomExerciseRepository customExerciseRepository;
    private final RoutineRepository routineRepository;
    private final AssignedRoutineRepository assignedRoutineRepository;
    private final RoutineService routineService;

    public SyncService(TrainerRepository trainerRepository,
                       StudentRepository studentRepository,
                       CustomExerciseRepository customExerciseRepository,
                       RoutineRepository routineRepository,
                       AssignedRoutineRepository assignedRoutineRepository,
                       RoutineService routineService) {
        this.trainerRepository = trainerRepository;
        this.studentRepository = studentRepository;
        this.customExerciseRepository = customExerciseRepository;
        this.routineRepository = routineRepository;
        this.assignedRoutineRepository = assignedRoutineRepository;
        this.routineService = routineService;
    }

    @Transactional
    public Map<String, Object> push(String trainerEmail, List<SyncOperation> operations) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var errors = new ArrayList<Map<String, Object>>();
        int processed = 0;

        for (var op : operations) {
            try {
                processOperation(trainer, op);
                processed++;
            } catch (Exception e) {
                errors.add(Map.of(
                    "operation", op.getOp(),
                    "entity", op.getEntity(),
                    "id", op.getId(),
                    "error", e.getMessage()
                ));
            }
        }

        return Map.of("processed", processed, "errors", errors);
    }

    public SyncPullResponse pull(String trainerEmail, LocalDateTime since) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var students = studentRepository.findByTrainerIdAndUpdatedAtAfter(trainer.getId(), since);
        var customExercises = customExerciseRepository.findByTrainerIdAndUpdatedAtAfter(trainer.getId(), since);
        var routines = routineRepository.findByTrainerIdAndUpdatedAtAfter(trainer.getId(), since);
        var assignedRoutines = assignedRoutineRepository.findByTrainerIdAndUpdatedAtAfter(trainer.getId(), since);

        return new SyncPullResponse(
            LocalDateTime.now(),
            students,
            customExercises.stream().map(CustomExerciseResponse::from).toList(),
            routines.stream().map(r -> {
                var dto = new RoutineResponse();
                dto.setId(r.getId());
                dto.setName(r.getName());
                dto.setDescription(r.getDescription());
                dto.setCoachName(r.getCoachName());
                dto.setCreatedAt(r.getCreatedAt());
                dto.setUpdatedAt(r.getUpdatedAt());
                return dto;
            }).toList(),
            assignedRoutines.stream().map(this::toAssignedResponse).toList()
        );
    }

    @SuppressWarnings("unchecked")
    private void processOperation(Trainer trainer, SyncOperation op) {
        switch (op.getEntity()) {
            case "student" -> processStudent(trainer, op);
            case "custom_exercise" -> processCustomExercise(trainer, op);
            case "routine" -> processRoutine(trainer, op);
            case "assigned_routine" -> processAssignedRoutine(trainer, op);
            default -> throw new IllegalArgumentException("Entidad desconocida: " + op.getEntity());
        }
    }

    private void processStudent(Trainer trainer, SyncOperation op) {
        var data = op.getData();
        switch (op.getOp()) {
            case "CREATE" -> {
                var s = new Student();
                s.setTrainer(trainer);
                applyStudentData(s, data);
                if (op.getId() != null) s.setId(op.getId());
                studentRepository.save(s);
            }
            case "UPDATE" -> {
                var s = findStudent(trainer, op.getId());
                applyStudentData(s, data);
                studentRepository.save(s);
            }
            case "DELETE" -> studentRepository.delete(findStudent(trainer, op.getId()));
        }
    }

    private void processCustomExercise(Trainer trainer, SyncOperation op) {
        var data = op.getData();
        switch (op.getOp()) {
            case "CREATE" -> {
                var ce = new CustomExercise();
                ce.setTrainer(trainer);
                applyCustomExerciseData(ce, data);
                if (op.getId() != null) ce.setId(op.getId());
                customExerciseRepository.save(ce);
            }
            case "UPDATE" -> {
                var ce = findCustomExercise(trainer, op.getId());
                applyCustomExerciseData(ce, data);
                customExerciseRepository.save(ce);
            }
            case "DELETE" -> customExerciseRepository.delete(findCustomExercise(trainer, op.getId()));
        }
    }

    private void processRoutine(Trainer trainer, SyncOperation op) {
        switch (op.getOp()) {
            case "DELETE" -> routineRepository.delete(findRoutine(trainer, op.getId()));
            default -> throw new IllegalArgumentException("Operación no soportada para routine: " + op.getOp());
        }
    }

    private void processAssignedRoutine(Trainer trainer, SyncOperation op) {
        switch (op.getOp()) {
            case "DELETE" -> assignedRoutineRepository.delete(findAssignedRoutine(trainer, op.getId()));
            default -> throw new IllegalArgumentException("Operación no soportada para assigned_routine: " + op.getOp());
        }
    }

    private void applyStudentData(Student s, Map<String, Object> data) {
        if (data == null) return;
        s.setFullName(str(data.get("fullName")));
        s.setPhoneNumber(str(data.get("phoneNumber")));
        s.setAge(data.get("age") instanceof Number n ? n.intValue() : null);
        s.setGender(str(data.get("gender")));
        s.setDisciplina(str(data.get("disciplina")));
        s.setObjetivos(str(data.get("objetivos")));
    }

    private void applyCustomExerciseData(CustomExercise ce, Map<String, Object> data) {
        if (data == null) return;
        ce.setName(str(data.get("name")));
        ce.setMuscle(str(data.get("muscle")));
        ce.setEquipment(str(data.get("equipment")));
        ce.setGifUrl(str(data.get("gifUrl")));
        ce.setVideoUrl(str(data.get("videoUrl")));
        ce.setTarget(str(data.get("target")));
        ce.setSecondaryMuscles(data.get("secondaryMuscles") != null ? data.get("secondaryMuscles").toString() : "[]");
        ce.setInstructions(data.get("instructions") != null ? data.get("instructions").toString() : "[]");
    }

    private Student findStudent(Trainer trainer, String id) {
        var s = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + id));
        if (!s.getTrainer().getId().equals(trainer.getId()))
            throw new IllegalArgumentException("Estudiante no pertenece al entrenador");
        return s;
    }

    private CustomExercise findCustomExercise(Trainer trainer, String id) {
        var ce = customExerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ejercicio personalizado no encontrado: " + id));
        if (!ce.getTrainer().getId().equals(trainer.getId()))
            throw new IllegalArgumentException("Ejercicio no pertenece al entrenador");
        return ce;
    }

    private Routine findRoutine(Trainer trainer, String id) {
        var r = routineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rutina no encontrada: " + id));
        if (!r.getTrainer().getId().equals(trainer.getId()))
            throw new IllegalArgumentException("Rutina no pertenece al entrenador");
        return r;
    }

    private AssignedRoutine findAssignedRoutine(Trainer trainer, String id) {
        var ar = assignedRoutineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rutina asignada no encontrada: " + id));
        if (!ar.getTrainer().getId().equals(trainer.getId()))
            throw new IllegalArgumentException("Rutina asignada no pertenece al entrenador");
        return ar;
    }

    private AssignedRoutineResponse toAssignedResponse(AssignedRoutine ar) {
        return new AssignedRoutineResponse(
            ar.getId(), ar.getName(), ar.getDescription(),
            ar.getStudent().getId(), ar.getStudentName(), ar.getStudentGoal(),
            ar.getCoachName(), ar.getShareUrl(), null, ar.getCreatedAt()
        );
    }

    private String str(Object v) { return v != null ? v.toString() : null; }
}
