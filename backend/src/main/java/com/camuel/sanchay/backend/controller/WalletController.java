package com.camuel.sanchay.backend.controller;

import com.camuel.sanchay.backend.dto.ApiResponse;
import com.camuel.sanchay.backend.dto.WalletDto;
import com.camuel.sanchay.backend.security.UserPrincipal;
import com.camuel.sanchay.backend.service.WalletService;
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
@RequestMapping("/api/v1/wallets")
@Tag(name = "Wallets", description = "Multi-wallet balance management")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    @Operation(summary = "Get all wallets for user")
    public ResponseEntity<ApiResponse<List<WalletDto>>> getWallets(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWalletsForUser(principal.getId())));
    }

    @PostMapping
    @Operation(summary = "Create new wallet")
    public ResponseEntity<ApiResponse<WalletDto>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WalletDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created", walletService.createWallet(principal.getId(), dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update wallet")
    public ResponseEntity<ApiResponse<WalletDto>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody WalletDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Wallet updated", walletService.updateWallet(principal.getId(), id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete wallet")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        walletService.deleteWallet(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Wallet deleted", null));
    }
}
