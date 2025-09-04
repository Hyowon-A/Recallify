package com.andy.recallify.mcq.service;

import com.andy.recallify.mcq.McqScore;
import com.andy.recallify.mcq.McqSet;
import com.andy.recallify.mcq.dto.McqScoreDto;
import com.andy.recallify.mcq.repository.McqScoreRepository;
import com.andy.recallify.mcq.repository.McqSetRepository;
import com.andy.recallify.user.User;
import com.andy.recallify.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class McqScoreService {
    private final McqSetRepository mcqSetRepository;
    private final McqScoreRepository mcqScoreRepository;
    private final UserRepository userRepository;

    public McqScoreService(McqSetRepository mcqSetRepository, McqScoreRepository mcqScoreRepository, UserRepository userRepository) {
        this.mcqSetRepository = mcqSetRepository;
        this.mcqScoreRepository = mcqScoreRepository;
        this.userRepository = userRepository;
    }

    public void store(McqScoreDto dto, String email) {
        McqSet set = mcqSetRepository.findById(dto.mcqSetId())
                .orElseThrow(() -> new RuntimeException("Set not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        McqScore score = new McqScore();
        score.setMcqSet(set);
        score.setUser(user);
        score.setScore(dto.score());
        score.setTakenAt(Timestamp.valueOf(LocalDateTime.now()));

        mcqScoreRepository.save(score);
    }

    public List<McqScore> getScoresById(Long setId) {
        return mcqScoreRepository.findByMcqSetId(setId);
    }
}
