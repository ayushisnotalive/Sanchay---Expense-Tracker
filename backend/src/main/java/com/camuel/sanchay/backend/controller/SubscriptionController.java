package com.camuel.sanchay.backend.controller;

import com.camuel.sanchay.backend.dto.ApiResponse;
import com.camuel.sanchay.backend.dto.SubscriptionDto;
import com.camuel.sanchay.backend.security.UserPrincipal;
import com.camuel.sanchay.backend.service.SubscriptionService;
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
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "Recurring bills and subscriptions")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    @Operation(summary = "Get all recurring subscriptions")
    public ResponseEntity<ApiResponse<List<SubscriptionDto>>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getSubscriptions(principal.getId())));
    }

    @PostMapping
    @Operation(summary = "Create recurring subscription")
    public ResponseEntity<ApiResponse<SubscriptionDto>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SubscriptionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription created", subscriptionService.createSubscription(principal.getId(), dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete subscription")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        subscriptionService.deleteSubscription(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Subscription deleted", null));
    }
}
