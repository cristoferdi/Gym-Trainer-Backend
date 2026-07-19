package com.softech.entrenaback.assigned.dto;

import jakarta.validation.constraints.NotBlank;

public class AssignRoutineRequest {

    @NotBlank
    private String routineTemplateId;

    @NotBlank
    private String studentId;

    private String coachName;

    public String getRoutineTemplateId() { return routineTemplateId; }
    public void setRoutineTemplateId(String routineTemplateId) { this.routineTemplateId = routineTemplateId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }
}
