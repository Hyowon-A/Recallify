package com.andy.recallify.mcq;

public class McqSetListInfoDto {
    private final Long id;
    private final String title;
    private final boolean isPublic;
    private final int numQuestions;

    public McqSetListInfoDto(Long id, String title, boolean isPublic, int numQuestions) {
        this.id = id;
        this.title = title;
        this.isPublic = isPublic;
        this.numQuestions = numQuestions;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public int getNumQuestions() {
        return numQuestions;
    }
}

