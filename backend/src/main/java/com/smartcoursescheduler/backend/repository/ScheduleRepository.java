package com.smartcoursescheduler.backend.repository;

import com.smartcoursescheduler.backend.persistence.entity.Schedule;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    Optional<Schedule> findFirstByOrderByRequestedAtDesc();

    default Optional<Schedule> findLatestByUserId() {
        return findFirstByOrderByRequestedAtDesc();
    }
}
