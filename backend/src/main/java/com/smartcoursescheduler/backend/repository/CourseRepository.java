package com.smartcoursescheduler.backend.repository;

import com.smartcoursescheduler.backend.persistence.entity.Course;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {}
