package com.andy.recallify.set.repository;

import com.andy.recallify.set.model.FlashcardSRS;
import com.andy.recallify.set.model.McqSRS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface McqSRSRepository extends JpaRepository<McqSRS, Long> {

    @Query("SELECT s FROM McqSRS s " +
            "JOIN s.mcq f " +
            "WHERE f.set.id = :setId " +
            "AND (s.lastReviewedAt IS NULL)")
    List<McqSRS> findNewCardsBySetId(Long setId);

    @Query("SELECT s FROM McqSRS s " +
            "JOIN s.mcq f " +
            "WHERE f.set.id = :setId " +
            "AND (s.repetitions <= 3) AND (s.lastReviewedAt IS NOT NULL)")
    List<McqSRS> findLearnCardsBySetId(Long setId);

    @Query("SELECT s FROM McqSRS s " +
            "JOIN s.mcq f " +
            "WHERE f.set.id = :setId " +
            "AND s.repetitions > 2 " +
            "AND s.nextReviewAt <= CURRENT_TIMESTAMP")
    List<McqSRS> findDueCardsBySetId(Long setId);

}
