package com.andy.recallify.mcq.controller;

import com.andy.recallify.mcq.McqScore;
import com.andy.recallify.mcq.dto.McqScoreDto;
import com.andy.recallify.mcq.repository.McqScoreRepository;
import com.andy.recallify.mcq.service.McqScoreService;
import com.andy.recallify.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path="api/mcqScore")
public class McqScoreController {
    private final JwtUtil jwtUtil;

    private final McqScoreService mcqScoreService;

    @Autowired
    public McqScoreController(JwtUtil jwtUtil, McqScoreService mcqScoreService) {
        this.jwtUtil = jwtUtil;
        this.mcqScoreService = mcqScoreService;
    }

    @PostMapping("/store")
    public ResponseEntity<?> storeScore(@RequestBody McqScoreDto dto, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            mcqScoreService.store(dto, email);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("get/{setId}")
    public ResponseEntity<?> getScore(@PathVariable("setId") Long setId) {
        try {
            List<McqScore> scores = mcqScoreService.getScoresById(setId);
            List<Map<String, Serializable>> result = scores.stream()
                    .map(s -> Map.<String, Serializable>of(
                            "score", s.getScore(),
                            "takenAt", s.getTakenAt()
                    ))
                    .toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

}
