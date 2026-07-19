package com.softech.entrenaback.exercise.dto;

import java.util.List;

public class MuscleEquipmentResponse {

    private List<Item> data;

    public MuscleEquipmentResponse(List<Item> data) {
        this.data = data;
    }

    public List<Item> getData() { return data; }

    public record Item(String key, String displayName) {}
}
