package com.andy.recallify.set.model;

import jakarta.persistence.*;

import java.sql.Time;
import java.sql.Timestamp;

@Entity
@Table(name = "flashcard_SRS")
public class FlashcardSRS {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    private int interval_hours;
    private int repetitions;
    private float ef;   // Easiness Factor
    private Timestamp lastReviewedAt;
    private Timestamp nextReviewAt;
    private Timestamp createdAt;

    public Long getId() {
        return id;
    }

    public Flashcard getFlashcard() {
        return flashcard;
    }

    public void setFlashcard(Flashcard flashcard) {
        this.flashcard = flashcard;
    }

    public int getInterval_hours() {
        return interval_hours;
    }

    public void setInterval_hours(int interval_hours) {
        this.interval_hours = interval_hours;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public float getEf() {
        return ef;
    }

    public void setEf(float ef) {
        this.ef = ef;
    }

    public Timestamp getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(Timestamp lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public Timestamp getNextReviewAt() {
        return nextReviewAt;
    }

    public void setNextReviewAt(Timestamp nextReviewAt) {
        this.nextReviewAt = nextReviewAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
