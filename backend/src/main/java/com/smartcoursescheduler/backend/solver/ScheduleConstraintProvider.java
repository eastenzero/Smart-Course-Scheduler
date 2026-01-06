package com.smartcoursescheduler.backend.solver;

import com.smartcoursescheduler.backend.solver.domain.ConstraintType;
import com.smartcoursescheduler.backend.solver.domain.Lesson;
import com.smartcoursescheduler.backend.solver.domain.PreferenceConstraint;
import java.time.DayOfWeek;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

public class ScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            roomConflict(constraintFactory),
            teacherConflict(constraintFactory),
            studentGroupConflict(constraintFactory),
            noMorningClasses(constraintFactory),
            noFriday(constraintFactory)
        };
    }

    Constraint roomConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(
                        Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getRoom))
                .filter((a, b) -> a.getTimeslot() != null && a.getRoom() != null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Room conflict");
    }

    Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(
                        Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getTeacher))
                .filter((a, b) -> a.getTimeslot() != null && a.getTeacher() != null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

    Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(
                        Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getStudentGroup))
                .filter((a, b) -> a.getTimeslot() != null && a.getStudentGroup() != null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Student group conflict");
    }

    Constraint noMorningClasses(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(PreferenceConstraint.class)
                .filter(c -> c.getType() == ConstraintType.NO_MORNING_CLASSES)
                .join(Lesson.class)
                .filter((c, lesson) ->
                        lesson.getTimeslot() != null
                                && lesson.getTimeslot().getStartTime() != null
                                && lesson.getTimeslot().getStartTime().isBefore(c.getMorningEnd()))
                .penalize(HardSoftScore.ONE_SOFT, (c, lesson) -> Math.max(0, c.getWeight()))
                .asConstraint("No morning classes");
    }

    Constraint noFriday(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(PreferenceConstraint.class)
                .filter(c -> c.getType() == ConstraintType.NO_FRIDAY)
                .join(Lesson.class)
                .filter((c, lesson) ->
                        lesson.getTimeslot() != null
                                && lesson.getTimeslot().getDayOfWeek() != null
                                && lesson.getTimeslot().getDayOfWeek() == DayOfWeek.of(c.getBannedDayOfWeek()))
                .penalize(HardSoftScore.ONE_SOFT, (c, lesson) -> Math.max(0, c.getWeight()))
                .asConstraint("No Friday");
    }
}
