package com.softech.entrenaback.assigned.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class AssignedRoutineResponse {

    private String id;
    private String name;
    private String description;

    @JsonProperty("studentId")
    private String studentId;

    @JsonProperty("studentName")
    private String studentName;

    @JsonProperty("studentGoal")
    private String studentGoal;

    @JsonProperty("coachName")
    private String coachName;

    @JsonProperty("shareUrl")
    private String shareUrl;

    private List<AssignedDayDto> days;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    public AssignedRoutineResponse() {}

    public AssignedRoutineResponse(String id, String name, String description, String studentId,
                                   String studentName, String studentGoal, String coachName,
                                   String shareUrl, List<AssignedDayDto> days, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentGoal = studentGoal;
        this.coachName = coachName;
        this.shareUrl = shareUrl;
        this.days = days;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentGoal() { return studentGoal; }
    public String getCoachName() { return coachName; }
    public String getShareUrl() { return shareUrl; }
    public List<AssignedDayDto> getDays() { return days; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
