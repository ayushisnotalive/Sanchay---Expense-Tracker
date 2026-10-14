package com.camuel.sanchay.backend.controller;

import com.camuel.sanchay.backend.dto.ApiResponse;
import com.camuel.sanchay.backend.dto.SyncDto.PullResponse;
import com.camuel.sanchay.backend.dto.SyncDto.PushRequest;
import com.camuel.sanchay.backend.security.UserPrincipal;
import com.camuel.sanchay.backend.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/sync")
@Tag(name = "Sync Engine", description = "Offline-first delta synchronization between Android Room and Backend")
@SecurityRequirement(name = "bearerAuth")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/push")
    @Operation(summary = "Push dirty local records to cloud backend")
    public ResponseEntity<ApiResponse<Instant>> pushDelta(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody PushRequest request) {
        Instant timestamp = syncService.pushDelta(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Delta synced successfully", timestamp));
    }

    @GetMapping("/pull")
    @Operation(summary = "Pull cloud delta records updated since timestamp")
    public ResponseEntity<ApiResponse<PullResponse>> pullDelta(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        PullResponse response = syncService.pullDelta(principal.getId(), since);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
