package com.andy.recallify.set.repository;

import com.andy.recallify.set.model.Flashcard;
import com.andy.recallify.set.model.FlashcardSRS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FlashcardSRSRepository extends JpaRepository<FlashcardSRS, Long> {

    @Query("SELECT s FROM FlashcardSRS s " +
            "JOIN s.flashcard f " +
            "WHERE f.set.id = :setId " +
            "AND (s.lastReviewedAt IS NULL)")
    List<FlashcardSRS> findNewCardsBySetId(Long setId);

    @Query("SELECT s FROM FlashcardSRS s " +
            "JOIN s.flashcard f " +
            "WHERE f.set.id = :setId " +
            "AND (s.repetitions <= 3) AND (s.lastReviewedAt IS NOT NULL)")
    List<FlashcardSRS> findLearnCardsBySetId(Long setId);

    @Query("SELECT s FROM FlashcardSRS s " +
            "JOIN s.flashcard f " +
            "WHERE f.set.id = :setId " +
            "AND s.repetitions > 2 " +
            "AND s.nextReviewAt <= CURRENT_TIMESTAMP")
    List<FlashcardSRS> findDueCardsBySetId(Long setId);

}
