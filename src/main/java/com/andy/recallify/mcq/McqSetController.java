package com.andy.recallify.mcq;

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
            @RequestBody String mcqSetTitle,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            Long setId = mcqSetService.createSet(mcqSetTitle, email);
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

            List<McqSetListInfoDto> summaries = mcqSetService.getMyMcqSets(email);
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/public")
    public ResponseEntity<?> getPublicMcqSets() {
        try {
            List<McqSetListInfoDto> publicSets = mcqSetService.getPublicMcqSets();
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
}
