package com.camuel.sanchay.backend.controller;

import com.camuel.sanchay.backend.dto.ApiResponse;
import com.camuel.sanchay.backend.security.UserPrincipal;
import com.camuel.sanchay.backend.service.AiAdvisorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Financial Advisor", description = "Secure server-side financial advice and prompt evaluation")
@SecurityRequirement(name = "bearerAuth")
public class AiAdvisorController {

    private final AiAdvisorService aiAdvisorService;

    public AiAdvisorController(AiAdvisorService aiAdvisorService) {
        this.aiAdvisorService = aiAdvisorService;
    }

    @PostMapping("/advice")
    @Operation(summary = "Get AI financial advice based on recent spending context")
    public ResponseEntity<ApiResponse<String>> getAdvice(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> request) {

        String prompt = request.getOrDefault("prompt", "Analyze my spending habits and give advice.");
        String advice = aiAdvisorService.getFinancialAdvice(principal.getId(), prompt);
        return ResponseEntity.ok(ApiResponse.success(advice));
    }
}
