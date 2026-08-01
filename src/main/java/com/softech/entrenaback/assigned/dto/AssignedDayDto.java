package com.softech.entrenaback.assigned.dto;

import java.util.List;

public class AssignedDayDto {

    private String dayId;
    private String dayName;
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
