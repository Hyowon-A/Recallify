package com.andy.recallify.mcq.controller;

import com.andy.recallify.mcq.dto.EditMcqSetRequest;
import com.andy.recallify.mcq.service.SetService;
import com.andy.recallify.mcq.dto.SetDto;
import com.andy.recallify.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path="api/set")
public class SetController {

    private final SetService setService;

    private final JwtUtil jwtUtil;

    @Autowired
    public SetController(SetService setService, JwtUtil jwtUtil) {
        this.setService = setService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSet(
            @RequestBody SetDto setDto,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            Long setId = setService.createSet(setDto.title(), setDto.isPublic(), email);
            return ResponseEntity.ok().body(Map.of(
                    "id", setId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/mcq/my")
    public ResponseEntity<?> getMyMcqSets(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            List<SetDto> summaries = setService.getMyMcqSets(email);
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/mcq/public")
    public ResponseEntity<?> getPublicMcqSets() {
        try {
            List<SetDto> publicSets = setService.getPublicMcqSets();
            return ResponseEntity.ok(publicSets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/meta/{setId}")
    public ResponseEntity<?> getSetMetadata(@PathVariable Long setId) {
        try {
            SetDto meta = setService.getMcqSetById(setId);
            return ResponseEntity.ok(meta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{setId}")
    public ResponseEntity<?> deleteMcqSet(@PathVariable Long setId) {
        try {
            setService.deleteMcqSetById(setId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mcq/edit")
    public ResponseEntity<?> updateMcqSet(
            @RequestBody EditMcqSetRequest editMcqSetRequest,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            setService.editMcqSet(editMcqSetRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
}
