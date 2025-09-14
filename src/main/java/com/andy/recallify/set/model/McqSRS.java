package com.andy.recallify.set.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "mcq_SRS")
public class McqSRS {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mcq_id", nullable = false)
    private Mcq mcq;

    private float interval_hours;
    private int repetitions;
    private float ef;   // Easiness Factor
    private Timestamp lastReviewedAt;
    private Timestamp nextReviewAt;

    @CreationTimestamp
    @Column(updatable = false)
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
