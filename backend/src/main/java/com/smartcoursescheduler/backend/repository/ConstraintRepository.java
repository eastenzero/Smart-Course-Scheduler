package com.smartcoursescheduler.backend.repository;

import com.smartcoursescheduler.backend.persistence.entity.Constraint;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstraintRepository extends JpaRepository<Constraint, UUID> {}
