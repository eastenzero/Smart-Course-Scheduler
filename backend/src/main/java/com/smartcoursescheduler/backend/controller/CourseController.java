package com.smartcoursescheduler.backend.controller;

import com.smartcoursescheduler.backend.controller.dto.CreateConstraintRequest;
import com.smartcoursescheduler.backend.controller.dto.GenerateScheduleRequest;
import com.smartcoursescheduler.backend.controller.dto.GenerateScheduleResponse;
import com.smartcoursescheduler.backend.persistence.entity.Constraint;
import com.smartcoursescheduler.backend.persistence.entity.Course;
import com.smartcoursescheduler.backend.repository.ConstraintRepository;
import com.smartcoursescheduler.backend.repository.CourseRepository;
import com.smartcoursescheduler.backend.solver.ScheduleSolverService;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CourseController {

    private final CourseRepository courseRepository;
    private final ConstraintRepository constraintRepository;
    private final ScheduleSolverService scheduleSolverService;

    public CourseController(
            CourseRepository courseRepository,
            ConstraintRepository constraintRepository,
            ScheduleSolverService scheduleSolverService) {
        this.courseRepository = courseRepository;
        this.constraintRepository = constraintRepository;
        this.scheduleSolverService = scheduleSolverService;
    }

    @GetMapping("/courses")
    public List<Course> listCourses() {
        return courseRepository.findAll();
    }

    @PostMapping("/constraints")
    @ResponseStatus(HttpStatus.CREATED)
    public Constraint createConstraint(@Valid @RequestBody CreateConstraintRequest request) {
        Constraint entity = new Constraint();
        entity.setConstraintKey(request.getConstraintKey());
        entity.setWeight(request.getWeight());
        entity.setEnabled(request.getEnabled() == null || request.getEnabled());
        entity.setParams(request.getParams());
        return constraintRepository.save(entity);
    }

    @PostMapping("/schedule/generate")
    public GenerateScheduleResponse generateSchedule(@RequestBody(required = false) GenerateScheduleRequest request) {
        UUID userId = request == null ? null : request.getUserId();
        UUID scheduleId = scheduleSolverService.generateSchedule(userId);
        return new GenerateScheduleResponse(scheduleId);
    }
}
