package com.smartcoursescheduler.backend.solver.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@PlanningEntity
public class Lesson {

    @PlanningId
    private UUID id;

    private UUID courseId;
    private String courseName;
    private String teacher;
    private String studentGroup;

    @PlanningVariable(valueRangeProviderRefs = "timeslotRange")
    private Timeslot timeslot;

    @PlanningVariable(valueRangeProviderRefs = "roomRange")
    private Room room;

    public Lesson(UUID id, UUID courseId, String courseName, String teacher, String studentGroup) {
        this.id = id;
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacher = teacher;
        this.studentGroup = studentGroup;
    }
}
