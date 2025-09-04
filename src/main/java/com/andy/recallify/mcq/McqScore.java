package com.andy.recallify.mcq;


import com.andy.recallify.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "mcq_scores")
public class McqScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "mcq_set_id", nullable = false)
    private McqSet mcqSet;

    private int score;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp takenAt;

    public Timestamp getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(Timestamp takenAt) {
        this.takenAt = takenAt;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public McqSet getMcqSet() {
        return mcqSet;
    }

    public void setMcqSet(McqSet mcqSet) {
        this.mcqSet = mcqSet;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
