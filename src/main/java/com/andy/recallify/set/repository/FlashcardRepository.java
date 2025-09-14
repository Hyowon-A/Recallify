package com.andy.recallify.set.repository;

import com.andy.recallify.set.model.Flashcard;
import com.andy.recallify.set.model.Mcq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findBySetId(Long setId);

    @Query("SELECT COUNT(f) FROM Flashcard f WHERE f.set.id = :setId")
    int countFlashcardsBySetId(@Param("setId") Long setId);
}
