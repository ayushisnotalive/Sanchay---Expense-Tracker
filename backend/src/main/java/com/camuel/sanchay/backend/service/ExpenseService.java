package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.ExpenseDto;
import com.camuel.sanchay.backend.entity.AuditLog;
import com.camuel.sanchay.backend.entity.Expense;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.exception.ResourceNotFoundException;
import com.camuel.sanchay.backend.repository.AuditLogRepository;
import com.camuel.sanchay.backend.repository.ExpenseRepository;
import com.camuel.sanchay.backend.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository, AuditLogRepository auditLogRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public List<ExpenseDto> getAllExpenses(Long userId) {
        User user = getUser(userId);
        return expenseRepository.findByUserAndDeletedFalseOrderByDateDesc(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "analyticsSummary", key = "#userId")
    public ExpenseDto createExpense(Long userId, ExpenseDto dto) {
        User user = getUser(userId);

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setClientUuid(dto.getClientUuid() != null ? dto.getClientUuid() : UUID.randomUUID().toString());
        mapFromDto(dto, expense);

        Expense saved = expenseRepository.save(expense);
        auditLogRepository.save(new AuditLog(userId, "CREATE", "Expense", saved.getId(), "Created expense of " + saved.getAmount()));
        return mapToDto(saved);
    }

    @Transactional
    @CacheEvict(value = "analyticsSummary", key = "#userId")
    public ExpenseDto updateExpense(Long userId, Long expenseId, ExpenseDto dto) {
        User user = getUser(userId);
        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        mapFromDto(dto, expense);
        Expense updated = expenseRepository.save(expense);
        auditLogRepository.save(new AuditLog(userId, "UPDATE", "Expense", updated.getId(), "Updated expense"));
        return mapToDto(updated);
    }

    @Transactional
    @CacheEvict(value = "analyticsSummary", key = "#userId")
    public void deleteExpense(Long userId, Long expenseId) {
        User user = getUser(userId);
        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        expense.setDeleted(true);
        expenseRepository.save(expense);
        auditLogRepository.save(new AuditLog(userId, "DELETE", "Expense", expenseId, "Soft deleted expense"));
    }

    public ExpenseDto mapToDto(Expense entity) {
        ExpenseDto dto = new ExpenseDto();
        dto.setId(entity.getId());
        dto.setClientUuid(entity.getClientUuid());
        dto.setAmount(entity.getAmount());
        dto.setCategory(entity.getCategory());
        dto.setDate(entity.getDate());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setNote(entity.getNote());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setWalletId(entity.getWalletId());
        dto.setTags(entity.getTags());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setDeleted(entity.isDeleted());
        return dto;
    }

    private void mapFromDto(ExpenseDto dto, Expense entity) {
        entity.setAmount(dto.getAmount());
        entity.setCategory(dto.getCategory());
        entity.setDate(dto.getDate());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setNote(dto.getNote());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        if (dto.getWalletId() != null) {
            entity.setWalletId(dto.getWalletId());
        }
        if (dto.getTags() != null) {
            entity.setTags(dto.getTags());
        }
    }
}
