package com.andy.recallify.mcq.repository;

import com.andy.recallify.mcq.McqScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreRepository extends JpaRepository<McqScore, Long> {
    List<McqScore> findBySetId(Long setId);
}
