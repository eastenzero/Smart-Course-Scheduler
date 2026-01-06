package com.smartcoursescheduler.backend.solver.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreferenceConstraint {

    private ConstraintType type;
    private int weight;
    private JsonNode params;

    public LocalTime getMorningEnd() {
        if (params == null) {
            return LocalTime.of(12, 0);
        }
        JsonNode node = params.get("morningEnd");
        if (node == null || node.isNull()) {
            node = params.get("earliestStart");
        }
        if (node == null || node.isNull() || !node.isTextual()) {
            return LocalTime.of(12, 0);
        }
        return LocalTime.parse(node.asText());
    }

    public int getBannedDayOfWeek() {
        if (params == null) {
            return 5;
        }
        JsonNode node = params.get("bannedDayOfWeek");
        if (node == null || node.isNull() || !node.canConvertToInt()) {
            return 5;
        }
        return node.asInt();
    }
}
