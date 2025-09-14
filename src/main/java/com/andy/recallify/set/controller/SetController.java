package com.andy.recallify.set.controller;

import com.andy.recallify.set.dto.EditSetRequest;
import com.andy.recallify.set.dto.PublicSetDto;
import com.andy.recallify.set.service.SetService;
import com.andy.recallify.set.dto.SetDto;
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

    @GetMapping("/my")
    public ResponseEntity<?> getMyMcqSets(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            List<SetDto> summaries = setService.getMySets(email);
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/public")
    public ResponseEntity<?> getPublicSets(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            List<SetDto> publicSets = setService.getPublicSets(email);
            return ResponseEntity.ok(publicSets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/meta/{setId}")
    public ResponseEntity<?> getSetMetadata(@PathVariable Long setId) {
        try {
            SetDto meta = setService.getSetById(setId);
            return ResponseEntity.ok(meta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{setId}")
    public ResponseEntity<?> deleteSet(@PathVariable Long setId) {
        try {
            setService.deleteSetById(setId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/edit")
    public ResponseEntity<?> updateSet(
            @RequestBody EditSetRequest editSetRequest,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            setService.editSet(editSetRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/copy/{sourceSetId}")
    public ResponseEntity<?> copySet(
            @PathVariable Long sourceSetId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);
            Long newSetId = setService.copySet(sourceSetId, email);
            return ResponseEntity.ok(Map.of("id", newSetId));
        } catch  (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
}
