package com.softech.entrenaback.routine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RoutineDayDto {

    @JsonProperty("day_id")
    private String dayId;

    @JsonProperty("day_name")
    private String dayName;

    private List<ExerciseBlockDto> blocks;

    @JsonProperty("order_index")
    private Integer orderIndex;

    public String getDayId() { return dayId; }
    public void setDayId(String dayId) { this.dayId = dayId; }

    public String getDayName() { return dayName; }
    public void setDayName(String dayName) { this.dayName = dayName; }

    public List<ExerciseBlockDto> getBlocks() { return blocks; }
    public void setBlocks(List<ExerciseBlockDto> blocks) { this.blocks = blocks; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
