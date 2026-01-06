package com.smartcoursescheduler.backend.controller.dto;

import java.util.UUID;

public class GenerateScheduleResponse {

    private UUID scheduleId;

    public GenerateScheduleResponse() {}

    public GenerateScheduleResponse(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }
}
