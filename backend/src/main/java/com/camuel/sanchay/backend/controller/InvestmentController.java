package com.camuel.sanchay.backend.controller;

import com.camuel.sanchay.backend.dto.ApiResponse;
import com.camuel.sanchay.backend.dto.InvestmentDto;
import com.camuel.sanchay.backend.security.UserPrincipal;
import com.camuel.sanchay.backend.service.InvestmentService;
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
@RequestMapping("/api/v1/investments")
@Tag(name = "Investments", description = "Investment portfolio management")
@SecurityRequirement(name = "bearerAuth")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @GetMapping
    @Operation(summary = "Get all investments")
    public ResponseEntity<ApiResponse<List<InvestmentDto>>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(investmentService.getInvestments(principal.getId())));
    }

    @PostMapping
    @Operation(summary = "Add investment holding")
    public ResponseEntity<ApiResponse<InvestmentDto>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InvestmentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Investment added", investmentService.createInvestment(principal.getId(), dto)));
    }

    @PatchMapping("/{id}/price")
    @Operation(summary = "Update current price of investment")
    public ResponseEntity<ApiResponse<InvestmentDto>> updatePrice(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam Double price) {
        return ResponseEntity.ok(ApiResponse.success("Price updated", investmentService.updateCurrentPrice(principal.getId(), id, price)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete investment holding")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        investmentService.deleteInvestment(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Investment deleted", null));
    }
}
