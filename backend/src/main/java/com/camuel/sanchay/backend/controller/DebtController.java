package com.camuel.sanchay.backend.controller;

import com.camuel.sanchay.backend.dto.ApiResponse;
import com.camuel.sanchay.backend.dto.DebtDto;
import com.camuel.sanchay.backend.security.UserPrincipal;
import com.camuel.sanchay.backend.service.DebtService;
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
@RequestMapping("/api/v1/debts")
@Tag(name = "Debts", description = "Lent and borrowed debt tracking")
@SecurityRequirement(name = "bearerAuth")
public class DebtController {

    private final DebtService debtService;

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    @GetMapping
    @Operation(summary = "Get all debts (lent & borrowed)")
    public ResponseEntity<ApiResponse<List<DebtDto>>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(debtService.getAllDebts(principal.getId())));
    }

    @PostMapping
    @Operation(summary = "Create new debt record")
    public ResponseEntity<ApiResponse<DebtDto>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DebtDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Debt record created", debtService.createDebt(principal.getId(), dto)));
    }

    @PatchMapping("/{id}/settle")
    @Operation(summary = "Toggle settled status of debt")
    public ResponseEntity<ApiResponse<DebtDto>> toggleSettled(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Settlement updated", debtService.toggleSettled(principal.getId(), id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete debt record")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        debtService.deleteDebt(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Debt record deleted", null));
    }
}
