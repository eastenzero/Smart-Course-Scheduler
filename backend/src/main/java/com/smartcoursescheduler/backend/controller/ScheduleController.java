package com.smartcoursescheduler.backend.controller;

import com.smartcoursescheduler.backend.persistence.entity.Schedule;
import com.smartcoursescheduler.backend.persistence.entity.ScheduleStatus;
import com.smartcoursescheduler.backend.repository.ScheduleRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;

    public ScheduleController(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus(@RequestParam("scheduleId") UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scheduleId", schedule.getId());
        payload.put("status", mapStatus(schedule.getStatus()));

        if (schedule.getScore() != null && !schedule.getScore().isEmpty()) {
            payload.put("score", schedule.getScore());
        }
        if (schedule.getResult() != null && !schedule.getResult().isEmpty()) {
            payload.put("result", schedule.getResult());
        }
        if (schedule.getErrorMessage() != null) {
            payload.put("errorMessage", schedule.getErrorMessage());
        }

        return payload;
    }

    @GetMapping("/result")
    public Map<String, Object> getResult(@RequestParam("scheduleId") UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("schedule", toSchedulePayload(schedule));
        return wrapper;
    }

    private Map<String, Object> toSchedulePayload(Schedule schedule) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", schedule.getId());
        payload.put("status", mapStatus(schedule.getStatus()));
        payload.put("requestedAt", schedule.getRequestedAt());
        payload.put("startedAt", schedule.getStartedAt());
        payload.put("finishedAt", schedule.getFinishedAt());
        payload.put("inputSnapshot", schedule.getInputSnapshot());
        payload.put("score", schedule.getScore());
        payload.put("result", schedule.getResult());
        payload.put("errorMessage", schedule.getErrorMessage());
        return payload;
    }

    private String mapStatus(ScheduleStatus status) {
        if (status == null) {
            return "SOLVING";
        }
        return switch (status) {
            case QUEUED, RUNNING -> "SOLVING";
            case COMPLETED -> "SOLVED";
            case FAILED -> "ERROR";
        };
    }
}
