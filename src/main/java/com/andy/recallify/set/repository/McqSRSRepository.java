package com.andy.recallify.set.repository;

import com.andy.recallify.set.dto.SetStatsDto;
import com.andy.recallify.set.model.FlashcardSRS;
import com.andy.recallify.set.model.McqSRS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface McqSRSRepository extends JpaRepository<McqSRS, Long> {

    @Query("SELECT s FROM McqSRS s " +
            "JOIN s.mcq f " +
            "WHERE f.set.id = :setId " +
            "AND (s.lastReviewedAt IS NULL)")
    List<McqSRS> findNewCardsBySetId(Long setId);

    @Query("SELECT s FROM McqSRS s " +
            "JOIN s.mcq f " +
            "WHERE f.set.id = :setId " +
            "AND (s.repetitions <= 2) AND (s.lastReviewedAt IS NOT NULL)")
    List<McqSRS> findLearnCardsBySetId(Long setId);

    @Query("SELECT s FROM McqSRS s " +
            "JOIN s.mcq f " +
            "WHERE f.set.id = :setId " +
            "AND s.repetitions > 2 " +
            "AND s.nextReviewAt <= CURRENT_TIMESTAMP")
    List<McqSRS> findDueCardsBySetId(Long setId);

    @Query("""
        SELECT new com.andy.recallify.set.dto.SetStatsDto(
            SUM(CASE WHEN s.lastReviewedAt IS NULL THEN 1 ELSE 0 END),
            SUM(CASE WHEN s.repetitions <= 2 AND s.lastReviewedAt IS NOT NULL THEN 1 ELSE 0 END),
            SUM(CASE WHEN s.repetitions > 2 AND s.nextReviewAt <= CURRENT_TIMESTAMP THEN 1 ELSE 0 END)
        )
        FROM McqSRS s JOIN s.mcq m WHERE m.set.id = :setId
    """)
    SetStatsDto countMcqStats(@Param("setId") Long setId);

    Optional<McqSRS> findByMcqId(Long mcqId);
}
