package com.andy.recallify.mcq;

import com.andy.recallify.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sets")
public class Set {
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

    @OneToMany(mappedBy = "set", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Mcq> mcqs = new ArrayList<>();

    public List<Mcq> getMcqs() {
        return mcqs;
    }

    public void setMcqs(List<Mcq> mcqs) {
        this.mcqs = mcqs;
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
