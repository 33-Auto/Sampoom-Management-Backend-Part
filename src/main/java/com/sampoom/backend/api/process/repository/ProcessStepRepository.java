package com.sampoom.backend.api.process.repository;

import com.sampoom.backend.api.process.entity.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessStepRepository extends JpaRepository<ProcessStep, Long> {
}

