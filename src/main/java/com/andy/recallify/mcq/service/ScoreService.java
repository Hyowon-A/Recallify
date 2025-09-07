package com.andy.recallify.mcq.service;

import com.andy.recallify.mcq.McqScore;
import com.andy.recallify.mcq.Set;
import com.andy.recallify.mcq.dto.ScoreDto;
import com.andy.recallify.mcq.repository.ScoreRepository;
import com.andy.recallify.mcq.repository.SetRepository;
import com.andy.recallify.user.User;
import com.andy.recallify.user.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScoreService {
    private final SetRepository setRepository;
    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;

    public ScoreService(SetRepository setRepository, ScoreRepository scoreRepository, UserRepository userRepository) {
        this.setRepository = setRepository;
        this.scoreRepository = scoreRepository;
        this.userRepository = userRepository;
    }

    public void store(ScoreDto dto, String email) {
        Set set = setRepository.findById(dto.setId())
                .orElseThrow(() -> new RuntimeException("Set not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        McqScore score = new McqScore();
        score.setSet(set);
        score.setUser(user);
        score.setScore(dto.score());
        score.setTakenAt(Timestamp.valueOf(LocalDateTime.now()));

        scoreRepository.save(score);
    }

    public List<McqScore> getScoresById(Long setId) {
        return scoreRepository.findBySetId(setId);
    }
}
