package com.softech.entrenaback.sync.dto;

import com.softech.entrenaback.assigned.dto.AssignedRoutineResponse;
import com.softech.entrenaback.customexercise.dto.CustomExerciseResponse;
import com.softech.entrenaback.routine.dto.RoutineResponse;
import com.softech.entrenaback.student.Student;

import java.time.LocalDateTime;
import java.util.List;

public class SyncPullResponse {

    private LocalDateTime timestamp;
    private List<Student> students;
    private List<CustomExerciseResponse> customExercises;
    private List<RoutineResponse> routines;
    private List<AssignedRoutineResponse> assignedRoutines;

    public SyncPullResponse(LocalDateTime timestamp, List<Student> students,
                            List<CustomExerciseResponse> customExercises,
                            List<RoutineResponse> routines,
                            List<AssignedRoutineResponse> assignedRoutines) {
        this.timestamp = timestamp;
        this.students = students;
        this.customExercises = customExercises;
        this.routines = routines;
        this.assignedRoutines = assignedRoutines;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public List<Student> getStudents() { return students; }
    public List<CustomExerciseResponse> getCustomExercises() { return customExercises; }
    public List<RoutineResponse> getRoutines() { return routines; }
    public List<AssignedRoutineResponse> getAssignedRoutines() { return assignedRoutines; }
}
