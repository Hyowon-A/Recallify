package com.andy.recallify.mcq;

import com.andy.recallify.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "mcq_sets")
public class McqSet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;

    private boolean isPublic;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "McqSet{" +
                "id=" + id +
                ", userId=" + user.getId() +
                ", title='" + title + '\'' +
                ", isPublic=" + isPublic +
                ", createdAt=" + createdAt +
                '}';
    }
}
