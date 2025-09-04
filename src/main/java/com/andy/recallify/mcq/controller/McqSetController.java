package com.andy.recallify.mcq.controller;

import com.andy.recallify.mcq.service.McqSetService;
import com.andy.recallify.mcq.dto.McqSetDto;
import com.andy.recallify.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path="api/mcqSet")
public class McqSetController {

    private final McqSetService mcqSetService;

    private final JwtUtil jwtUtil;

    @Autowired
    public McqSetController(McqSetService mcqSetService, JwtUtil jwtUtil) {
        this.mcqSetService = mcqSetService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createMcqSet(
            @RequestBody McqSetDto mcqSetDto,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            Long setId = mcqSetService.createSet(mcqSetDto.title(), mcqSetDto.isPublic(), email);
            return ResponseEntity.ok().body(Map.of(
                    "id", setId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyMcqSets(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            List<McqSetDto> summaries = mcqSetService.getMyMcqSets(email);
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/public")
    public ResponseEntity<?> getPublicMcqSets() {
        try {
            List<McqSetDto> publicSets = mcqSetService.getPublicMcqSets();
            return ResponseEntity.ok(publicSets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/meta/{setId}")
    public ResponseEntity<?> getSetMetadata(@PathVariable Long setId) {
        try {
            McqSetDto meta = mcqSetService.getMcqSetById(setId);
            return ResponseEntity.ok(meta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{setId}")
    public ResponseEntity<?> deleteMcqSet(@PathVariable Long setId) {
        try {
            mcqSetService.deleteMcqSetById(setId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
