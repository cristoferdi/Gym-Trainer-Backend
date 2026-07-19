package com.softech.entrenaback.assigned.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AssignedDayDto {

    @JsonProperty("day_id")
    private String dayId;

    @JsonProperty("day_name")
    private String dayName;

    @JsonProperty("order_index")
    private Integer orderIndex;

    private List<AssignedBlockDto> blocks;

    public String getDayId() { return dayId; }
    public void setDayId(String dayId) { this.dayId = dayId; }

    public String getDayName() { return dayName; }
    public void setDayName(String dayName) { this.dayName = dayName; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public List<AssignedBlockDto> getBlocks() { return blocks; }
    public void setBlocks(List<AssignedBlockDto> blocks) { this.blocks = blocks; }
}
