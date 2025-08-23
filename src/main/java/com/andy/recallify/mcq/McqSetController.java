package com.andy.recallify.mcq;

import com.andy.recallify.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody McqSet mcqSet,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            mcqSetService.createSet(mcqSet, email);
            return ResponseEntity.ok("Set created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
}
