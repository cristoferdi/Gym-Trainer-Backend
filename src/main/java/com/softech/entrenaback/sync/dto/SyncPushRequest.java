package com.softech.entrenaback.sync.dto;

import java.util.List;

public class SyncPushRequest {

    private String lastSyncTimestamp;
    private List<SyncOperation> operations;

    public String getLastSyncTimestamp() { return lastSyncTimestamp; }
    public void setLastSyncTimestamp(String lastSyncTimestamp) { this.lastSyncTimestamp = lastSyncTimestamp; }

    public List<SyncOperation> getOperations() { return operations; }
    public void setOperations(List<SyncOperation> operations) { this.operations = operations; }
}
