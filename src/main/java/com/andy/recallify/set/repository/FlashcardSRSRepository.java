package com.andy.recallify.set.repository;

import com.andy.recallify.set.dto.SetStatsDto;
import com.andy.recallify.set.model.Flashcard;
import com.andy.recallify.set.model.FlashcardSRS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlashcardSRSRepository extends JpaRepository<FlashcardSRS, Long> {

    @Query("SELECT s FROM FlashcardSRS s " +
            "JOIN s.flashcard f " +
            "WHERE f.set.id = :setId " +
            "AND (s.lastReviewedAt IS NULL)")
    List<FlashcardSRS> findNewCardsBySetId(Long setId);

    @Query("SELECT s FROM FlashcardSRS s " +
            "JOIN s.flashcard f " +
            "WHERE f.set.id = :setId " +
            "AND (s.repetitions <= 2) AND (s.lastReviewedAt IS NOT NULL)")
    List<FlashcardSRS> findLearnCardsBySetId(Long setId);

    @Query("SELECT s FROM FlashcardSRS s " +
            "JOIN s.flashcard f " +
            "WHERE f.set.id = :setId " +
            "AND s.repetitions > 2 " +
            "AND s.nextReviewAt <= CURRENT_TIMESTAMP")
    List<FlashcardSRS> findDueCardsBySetId(Long setId);

    @Query("""
        SELECT new com.andy.recallify.set.dto.SetStatsDto(
            f.set.id,
            SUM(CASE WHEN s.lastReviewedAt IS NULL THEN 1 ELSE 0 END),
            SUM(CASE WHEN s.repetitions <= 2 AND s.lastReviewedAt IS NOT NULL THEN 1 ELSE 0 END),
            SUM(CASE WHEN s.repetitions > 2 AND s.nextReviewAt <= CURRENT_TIMESTAMP THEN 1 ELSE 0 END)
        )
        FROM FlashcardSRS s
        JOIN s.flashcard f
        WHERE f.set.id IN :setIds
        GROUP BY f.set.id
    """)
    List<SetStatsDto> countFlashcardStatsGrouped(@Param("setIds") List<Long> setIds);

    @Query("""
        SELECT new com.andy.recallify.set.dto.SetStatsDto(
            f.set.id,
            COUNT(CASE WHEN s.lastReviewedAt IS NULL THEN 1 ELSE null END),
            COUNT(CASE WHEN s.repetitions <= 2 AND s.lastReviewedAt IS NOT NULL THEN 1 ELSE null END),
            COUNT(CASE WHEN s.repetitions > 2 AND s.nextReviewAt <= CURRENT_TIMESTAMP THEN 1 ELSE null END)
        )
        FROM FlashcardSRS s
        JOIN s.flashcard f
        WHERE f.set.id = :setId
        GROUP BY f.set.id
    """)
    SetStatsDto countFlashcardStats(@Param("setId") Long setId);

    Optional<FlashcardSRS> findByFlashcardId(Long flashcardId);

}
