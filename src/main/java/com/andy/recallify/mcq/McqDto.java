package com.andy.recallify.mcq;

public class McqDto {

    private String question;

    private String option1;
    private String explanation1;

    private String option2;
    private String explanation2;

    private String option3;
    private String explanation3;

    private String option4;
    private String explanation4;

    private int answer; // 1 to 4

    // --- Getters ---
    public String getQuestion() {
        return question;
    }

    public String getOption1() {
        return option1;
    }

    public String getExplanation1() {
        return explanation1;
    }

    public String getOption2() {
        return option2;
    }

    public String getExplanation2() {
        return explanation2;
    }

    public String getOption3() {
        return option3;
    }

    public String getExplanation3() {
        return explanation3;
    }

    public String getOption4() {
        return option4;
    }

    public String getExplanation4() {
        return explanation4;
    }

    public int getAnswer() {
        return answer;
    }

    // --- Setters ---
    public void setQuestion(String question) {
        this.question = question;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public void setExplanation1(String explanation1) {
        this.explanation1 = explanation1;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public void setExplanation2(String explanation2) {
        this.explanation2 = explanation2;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public void setExplanation3(String explanation3) {
        this.explanation3 = explanation3;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public void setExplanation4(String explanation4) {
        this.explanation4 = explanation4;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }
}