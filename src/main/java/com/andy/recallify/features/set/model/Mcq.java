package com.andy.recallify.features.set.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "mcqs")
public class Mcq {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private Set set;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String option1;

    @Column(columnDefinition = "TEXT")
    private String explanation1;

    @Column(columnDefinition = "TEXT")
    private String option2;

    @Column(columnDefinition = "TEXT")
    private String explanation2;

    @Column(columnDefinition = "TEXT")
    private String option3;

    @Column(columnDefinition = "TEXT")
    private String explanation3;

    @Column(columnDefinition = "TEXT")
    private String option4;

    @Column(columnDefinition = "TEXT")
    private String explanation4;

    @Column(name = "answer", nullable = false)
    private int answer; // 1 to 4

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @OneToOne(mappedBy = "mcq", fetch = FetchType.LAZY, orphanRemoval = true)
    private McqSRS mcqSRS;

    public long getId() {
        return id;
    }

    public Set getSet() {
        return set;
    }

    public void setSet(Set set) {
        this.set = set;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getExplanation1() {
        return explanation1;
    }

    public void setExplanation1(String explanation1) {
        this.explanation1 = explanation1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getExplanation2() {
        return explanation2;
    }

    public void setExplanation2(String explanation2) {
        this.explanation2 = explanation2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getExplanation3() {
        return explanation3;
    }

    public void setExplanation3(String explanation3) {
        this.explanation3 = explanation3;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public String getExplanation4() {
        return explanation4;
    }

    public void setExplanation4(String explanation4) {
        this.explanation4 = explanation4;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public McqSRS getMcqSRS() {
        return mcqSRS;
    }

    public void setMcqSRS(McqSRS mcqSRS) {
        this.mcqSRS = mcqSRS;
    }

    @Override
    public String toString() {
        return "Mcq{" +
                "id=" + id +
                ", mcqSet=" + set +
                ", question='" + question + '\'' +
                ", option1='" + option1 + '\'' +
                ", explanation1='" + explanation1 + '\'' +
                ", option2='" + option2 + '\'' +
                ", explanation2='" + explanation2 + '\'' +
                ", option3='" + option3 + '\'' +
                ", explanation3='" + explanation3 + '\'' +
                ", option4='" + option4 + '\'' +
                ", explanation4='" + explanation4 + '\'' +
                ", answer=" + answer +
                '}';
    }
}
