package com.softech.entrenaback.routine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CreateRoutineRequest {

    @NotBlank
    private String name;

    private String description;

    @NotEmpty
    private List<RoutineDayDto> days;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<RoutineDayDto> getDays() { return days; }
    public void setDays(List<RoutineDayDto> days) { this.days = days; }
}
