package com.andy.recallify.features.set.repository;

import com.andy.recallify.features.set.model.McqScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreRepository extends JpaRepository<McqScore, Long> {
    List<McqScore> findBySetId(Long setId);
}
