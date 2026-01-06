package com.smartcoursescheduler.backend.solver;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartcoursescheduler.backend.persistence.entity.Constraint;
import com.smartcoursescheduler.backend.persistence.entity.Course;
import com.smartcoursescheduler.backend.persistence.entity.Schedule;
import com.smartcoursescheduler.backend.persistence.entity.ScheduleStatus;
import com.smartcoursescheduler.backend.repository.ConstraintRepository;
import com.smartcoursescheduler.backend.repository.CourseRepository;
import com.smartcoursescheduler.backend.repository.ScheduleRepository;
import com.smartcoursescheduler.backend.solver.domain.ConstraintType;
import com.smartcoursescheduler.backend.solver.domain.CourseSchedule;
import com.smartcoursescheduler.backend.solver.domain.Lesson;
import com.smartcoursescheduler.backend.solver.domain.PreferenceConstraint;
import com.smartcoursescheduler.backend.solver.domain.Room;
import com.smartcoursescheduler.backend.solver.domain.Timeslot;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ScheduleSolverService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleSolverService.class);

    private final SolverManager<CourseSchedule, UUID> solverManager;
    private final CourseRepository courseRepository;
    private final ConstraintRepository constraintRepository;
    private final ScheduleRepository scheduleRepository;

    public ScheduleSolverService(
            SolverManager<CourseSchedule, UUID> solverManager,
            CourseRepository courseRepository,
            ConstraintRepository constraintRepository,
            ScheduleRepository scheduleRepository) {
        this.solverManager = solverManager;
        this.courseRepository = courseRepository;
        this.constraintRepository = constraintRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public UUID generateSchedule(UUID userId) {
        Schedule schedule = new Schedule();
        schedule.setStatus(ScheduleStatus.QUEUED);
        schedule.setRequestedAt(OffsetDateTime.now());
        schedule = scheduleRepository.save(schedule);

        UUID scheduleId = schedule.getId();
        CourseSchedule problem = buildProblem(userId);

        log.info(
                "Starting solver for scheduleId={} with {} lessons, {} timeslots, {} rooms, {} constraints",
                scheduleId,
                problem.getLessonList() == null ? 0 : problem.getLessonList().size(),
                problem.getTimeslotList() == null ? 0 : problem.getTimeslotList().size(),
                problem.getRoomList() == null ? 0 : problem.getRoomList().size(),
                problem.getPreferenceConstraintList() == null ? 0 : problem.getPreferenceConstraintList().size());

        SolverJob<CourseSchedule, UUID> solverJob = solverManager.solve(scheduleId, problem);

        CompletableFuture.runAsync(() -> {
            try {
                Schedule running = scheduleRepository.findById(scheduleId).orElseThrow();
                running.setStatus(ScheduleStatus.RUNNING);
                running.setStartedAt(OffsetDateTime.now());
                scheduleRepository.save(running);

                CourseSchedule solution = solverJob.getFinalBestSolution();
                persistSolution(scheduleId, solution);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                persistFailure(scheduleId, e);
            } catch (ExecutionException | RuntimeException e) {
                persistFailure(scheduleId, e);
            }
        });

        return scheduleId;
    }

    private CourseSchedule buildProblem(UUID userId) {
        List<Course> courses = courseRepository.findAll();
        List<Constraint> constraints = constraintRepository.findAll();

        List<Timeslot> timeslots = defaultTimeslots();
        List<Room> rooms = defaultRooms(courses);
        List<PreferenceConstraint> preferenceConstraints = toPreferenceConstraints(constraints);

        List<Lesson> lessons = new ArrayList<>();
        for (Course course : courses) {
            UUID lessonId = course.getId();
            String teacher = course.getTeacher() == null ? course.getId().toString() : course.getTeacher();
            lessons.add(new Lesson(lessonId, course.getId(), course.getName(), teacher, "DEFAULT"));
        }

        return new CourseSchedule(timeslots, rooms, preferenceConstraints, lessons);
    }

    private List<PreferenceConstraint> toPreferenceConstraints(List<Constraint> constraints) {
        List<PreferenceConstraint> list = new ArrayList<>();
        for (Constraint c : constraints) {
            if (!c.isEnabled()) {
                continue;
            }
            String key = c.getConstraintKey();
            if (key == null) {
                continue;
            }
            if ("NO_EARLY_CLASS".equals(key)) {
                key = "NO_MORNING_CLASSES";
            } else if ("MAXIMIZE_DAYS_OFF".equals(key)) {
                key = "MAX_DAYS_OFF";
            }
            ConstraintType type;
            try {
                type = ConstraintType.valueOf(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            list.add(new PreferenceConstraint(type, c.getWeight(), c.getParams()));
        }
        return list;
    }

    private void persistSolution(UUID scheduleId, CourseSchedule solution) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();

        ObjectNode scoreNode = JsonNodeFactory.instance.objectNode();
        if (solution.getScore() != null) {
            scoreNode.put("hard", solution.getScore().hardScore());
            scoreNode.put("soft", solution.getScore().softScore());
        }

        ObjectNode resultNode = JsonNodeFactory.instance.objectNode();
        var entries = JsonNodeFactory.instance.arrayNode();
        for (Lesson lesson : solution.getLessonList()) {
            if (lesson.getTimeslot() == null || lesson.getRoom() == null) {
                continue;
            }
            ObjectNode entry = JsonNodeFactory.instance.objectNode();
            entry.put("courseId", lesson.getCourseId() == null ? null : lesson.getCourseId().toString());
            entry.put("courseName", lesson.getCourseName());
            entry.put("teacher", lesson.getTeacher());
            entry.put("location", lesson.getRoom().getName());
            entry.put("dayOfWeek", lesson.getTimeslot().getDayOfWeek().getValue());
            entry.put("start", lesson.getTimeslot().getStartTime().toString());
            entry.put("end", lesson.getTimeslot().getEndTime().toString());
            entries.add(entry);
        }
        resultNode.set("entries", entries);

        schedule.setScore(scoreNode);
        schedule.setResult(resultNode);
        schedule.setStatus(ScheduleStatus.COMPLETED);
        schedule.setFinishedAt(OffsetDateTime.now());
        scheduleRepository.save(schedule);
    }

    private void persistFailure(UUID scheduleId, Exception e) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
        schedule.setStatus(ScheduleStatus.FAILED);
        schedule.setFinishedAt(OffsetDateTime.now());
        schedule.setErrorMessage(e.getMessage());
        scheduleRepository.save(schedule);
    }

    private List<Timeslot> defaultTimeslots() {
        List<LocalTime[]> slots = List.of(
                new LocalTime[] {LocalTime.of(8, 0), LocalTime.of(8, 45)},
                new LocalTime[] {LocalTime.of(8, 55), LocalTime.of(9, 40)},
                new LocalTime[] {LocalTime.of(10, 0), LocalTime.of(10, 45)},
                new LocalTime[] {LocalTime.of(10, 55), LocalTime.of(11, 40)},
                new LocalTime[] {LocalTime.of(13, 0), LocalTime.of(13, 45)},
                new LocalTime[] {LocalTime.of(13, 55), LocalTime.of(14, 40)},
                new LocalTime[] {LocalTime.of(15, 0), LocalTime.of(15, 45)},
                new LocalTime[] {LocalTime.of(15, 55), LocalTime.of(16, 40)},
                new LocalTime[] {LocalTime.of(17, 0), LocalTime.of(17, 45)},
                new LocalTime[] {LocalTime.of(17, 55), LocalTime.of(18, 40)},
                new LocalTime[] {LocalTime.of(19, 0), LocalTime.of(19, 45)},
                new LocalTime[] {LocalTime.of(19, 55), LocalTime.of(20, 40)})
        ;

        List<Timeslot> timeslots = new ArrayList<>();
        for (int day = 1; day <= 5; day++) {
            DayOfWeek dayOfWeek = DayOfWeek.of(day);
            for (LocalTime[] pair : slots) {
                timeslots.add(new Timeslot(dayOfWeek, pair[0], pair[1]));
            }
        }
        return timeslots;
    }

    private List<Room> defaultRooms(List<Course> courses) {
        Set<String> roomNames = new LinkedHashSet<>();
        roomNames.add("Room A");
        roomNames.add("Room B");
        for (Course course : courses) {
            if (course.getLocation() != null && !course.getLocation().isBlank()) {
                roomNames.add(course.getLocation());
            }
        }
        return roomNames.stream().map(Room::new).toList();
    }
}
