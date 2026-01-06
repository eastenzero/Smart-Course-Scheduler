package com.smartcoursescheduler.backend.controller.dto;

import java.util.UUID;

public class GenerateScheduleRequest {

    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
