package com.andy.recallify.features.set.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "mcq_srs")
public class McqSRS {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mcq_id", nullable = false, unique = true)
    private Mcq mcq;

    @Column(name = "interval_hours", nullable = false)
    private float interval_hours;

    @Column(name = "repetitions", nullable = false)
    private int repetitions;

    @Column(name = "ef", nullable = false)
    private float ef;   // Easiness Factor

    @Column(name = "last_reviewed_at")
    private Timestamp lastReviewedAt;

    @Column(name = "next_review_at")
    private Timestamp nextReviewAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    public McqSRS(Mcq mcq, float interval_hours, int repetitions, float ef, Timestamp lastReviewedAt, Timestamp nextReviewAt) {
        this.mcq = mcq;
        this.interval_hours = interval_hours;
        this.repetitions = repetitions;
        this.ef = ef;
        this.lastReviewedAt = lastReviewedAt;
        this.nextReviewAt = nextReviewAt;
    }

    public McqSRS() {

    }

    public Long getId() {
        return id;
    }

    public Mcq getMcq() {
        return mcq;
    }

    public void setMcq(Mcq mcq) {
        this.mcq = mcq;
    }

    public float getInterval_hours() {
        return interval_hours;
    }

    public void setInterval_hours(float interval_hours) {
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
