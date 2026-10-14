package com.camuel.sanchay.backend.controller;

import com.camuel.sanchay.backend.dto.ApiResponse;
import com.camuel.sanchay.backend.dto.SavingsGoalDto;
import com.camuel.sanchay.backend.security.UserPrincipal;
import com.camuel.sanchay.backend.service.SavingsGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/savings-goals")
@Tag(name = "Savings Goals", description = "Financial milestones and target goals")
@SecurityRequirement(name = "bearerAuth")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @GetMapping
    @Operation(summary = "Get all savings goals")
    public ResponseEntity<ApiResponse<List<SavingsGoalDto>>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(savingsGoalService.getSavingsGoals(principal.getId())));
    }

    @PostMapping
    @Operation(summary = "Create savings goal")
    public ResponseEntity<ApiResponse<SavingsGoalDto>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SavingsGoalDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created", savingsGoalService.createSavingsGoal(principal.getId(), dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update savings goal progress")
    public ResponseEntity<ApiResponse<SavingsGoalDto>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Goal updated", savingsGoalService.updateSavingsGoal(principal.getId(), id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete savings goal")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        savingsGoalService.deleteSavingsGoal(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Goal deleted", null));
    }
}
