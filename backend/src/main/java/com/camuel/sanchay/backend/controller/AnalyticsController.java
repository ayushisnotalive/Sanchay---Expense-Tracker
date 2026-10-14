package com.camuel.sanchay.backend.controller;

import com.camuel.sanchay.backend.dto.AnalyticsDto;
import com.camuel.sanchay.backend.dto.ApiResponse;
import com.camuel.sanchay.backend.security.UserPrincipal;
import com.camuel.sanchay.backend.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Aggregated spending statistics and budgeting metrics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get high-performance cached financial analytics summary")
    public ResponseEntity<ApiResponse<AnalyticsDto>> getSummary(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAnalyticsSummary(principal.getId())));
    }
}
