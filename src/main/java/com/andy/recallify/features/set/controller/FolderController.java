package com.andy.recallify.features.set.controller;

import com.andy.recallify.features.set.dto.CreateFolderRequest;
import com.andy.recallify.features.set.dto.EditFolderRequest;
import com.andy.recallify.features.set.dto.FolderDto;
import com.andy.recallify.features.set.dto.MoveSetFolderRequest;
import com.andy.recallify.features.set.dto.SetDto;
import com.andy.recallify.features.set.service.SetService;
import com.andy.recallify.shared.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "api/folder")
public class FolderController {
    private final SetService setService;
    private final JwtUtil jwtUtil;

    public FolderController(SetService setService, JwtUtil jwtUtil) {
        this.setService = setService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createFolder(
            @RequestBody CreateFolderRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String email = extractEmail(authHeader);
            Long folderId = setService.createFolder(request.title(), request.isPublic(), email);
            return ResponseEntity.ok(Map.of("id", folderId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyFolders(@RequestHeader("Authorization") String authHeader) {
        try {
            String email = extractEmail(authHeader);
            List<FolderDto> folders = setService.getMyFolders(email);
            return ResponseEntity.ok(folders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/edit")
    public ResponseEntity<?> editFolder(
            @RequestBody EditFolderRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String email = extractEmail(authHeader);
            setService.editFolder(request.folderId(), request.title(), request.isPublic(), email);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{folderId}")
    public ResponseEntity<?> deleteFolder(
            @PathVariable Long folderId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String email = extractEmail(authHeader);
            setService.deleteFolder(folderId, email);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/moveSet")
    public ResponseEntity<?> moveSetToFolder(
            @RequestBody MoveSetFolderRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String email = extractEmail(authHeader);
            setService.moveSetToFolder(request.setId(), request.folderId(), email);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{folderId}/sets")
    public ResponseEntity<?> getSetsInFolder(
            @PathVariable Long folderId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String email = extractEmail(authHeader);
            List<SetDto> sets = setService.getSetsInFolder(folderId, email);
            return ResponseEntity.ok(sets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    private String extractEmail(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractEmail(token);
    }
}
