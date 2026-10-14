package com.camuel.sanchay.backend.controller;

import com.camuel.sanchay.backend.dto.ApiResponse;
import com.camuel.sanchay.backend.dto.BackupPayloadDto;
import com.camuel.sanchay.backend.security.UserPrincipal;
import com.camuel.sanchay.backend.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/backup")
@Tag(name = "Backup & Data Portability", description = "Full enterprise JSON export and restoration")
@SecurityRequirement(name = "bearerAuth")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping("/export")
    @Operation(summary = "Export full database backup as JSON")
    public ResponseEntity<ApiResponse<BackupPayloadDto>> exportBackup(@AuthenticationPrincipal UserPrincipal principal) {
        BackupPayloadDto payload = backupService.exportBackup(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Backup generated successfully", payload));
    }

    @PostMapping("/import")
    @Operation(summary = "Import full database backup from JSON")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> importBackup(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody BackupPayloadDto payload) {
        Map<String, Integer> summary = backupService.importBackup(principal.getId(), payload);
        return ResponseEntity.ok(ApiResponse.success("Backup imported successfully", summary));
    }
}
