package com.softech.entrenaback.sync.dto;

import java.util.Map;

public class SyncOperation {

    private String op;
    private String entity;
    private String id;
    private Map<String, Object> data;

    public String getOp() { return op; }
    public void setOp(String op) { this.op = op; }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
