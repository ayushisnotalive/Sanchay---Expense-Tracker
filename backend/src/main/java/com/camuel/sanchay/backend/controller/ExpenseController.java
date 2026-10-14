package com.camuel.sanchay.backend.controller;

import com.camuel.sanchay.backend.dto.ApiResponse;
import com.camuel.sanchay.backend.dto.ExpenseDto;
import com.camuel.sanchay.backend.security.UserPrincipal;
import com.camuel.sanchay.backend.service.ExpenseService;
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
@RequestMapping("/api/v1/expenses")
@Tag(name = "Expenses", description = "Personal expense and transaction management")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    @Operation(summary = "Get all expenses for current user")
    public ResponseEntity<ApiResponse<List<ExpenseDto>>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        List<ExpenseDto> list = expenseService.getAllExpenses(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    @Operation(summary = "Create new expense")
    public ResponseEntity<ApiResponse<ExpenseDto>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ExpenseDto dto) {
        ExpenseDto created = expenseService.createExpense(principal.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Expense created", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing expense")
    public ResponseEntity<ApiResponse<ExpenseDto>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDto dto) {
        ExpenseDto updated = expenseService.updateExpense(principal.getId(), id, dto);
        return ResponseEntity.ok(ApiResponse.success("Expense updated", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        expenseService.deleteExpense(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
    }
}
