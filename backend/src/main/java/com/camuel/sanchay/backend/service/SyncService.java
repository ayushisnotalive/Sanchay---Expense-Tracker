package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.ExpenseDto;
import com.camuel.sanchay.backend.dto.SyncDto.PullResponse;
import com.camuel.sanchay.backend.dto.SyncDto.PushRequest;
import com.camuel.sanchay.backend.entity.AuditLog;
import com.camuel.sanchay.backend.entity.Expense;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.exception.ResourceNotFoundException;
import com.camuel.sanchay.backend.repository.AuditLogRepository;
import com.camuel.sanchay.backend.repository.ExpenseRepository;
import com.camuel.sanchay.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SyncService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;
    private final AuditLogRepository auditLogRepository;

    public SyncService(ExpenseRepository expenseRepository,
                       UserRepository userRepository,
                       ExpenseService expenseService,
                       AuditLogRepository auditLogRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.expenseService = expenseService;
        this.auditLogRepository = auditLogRepository;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @Transactional
    public Instant pushDelta(Long userId, PushRequest request) {
        User user = getUser(userId);
        Instant now = Instant.now();

        if (request.getExpenses() != null) {
            for (ExpenseDto dto : request.getExpenses()) {
                if (dto.getClientUuid() == null || dto.getClientUuid().isBlank()) {
                    continue;
                }

                Optional<Expense> existing = expenseRepository.findByClientUuidAndUser(dto.getClientUuid(), user);
                Expense entity;
                if (existing.isPresent()) {
                    entity = existing.get();
                } else {
                    entity = new Expense();
                    entity.setUser(user);
                    entity.setClientUuid(dto.getClientUuid());
                }

                entity.setAmount(dto.getAmount());
                entity.setCategory(dto.getCategory());
                entity.setDate(dto.getDate());
                entity.setPaymentMethod(dto.getPaymentMethod());
                entity.setNote(dto.getNote());
                entity.setLatitude(dto.getLatitude());
                entity.setLongitude(dto.getLongitude());
                if (dto.getWalletId() != null) entity.setWalletId(dto.getWalletId());
                if (dto.getTags() != null) entity.setTags(dto.getTags());
                entity.setDeleted(dto.isDeleted());

                expenseRepository.save(entity);
            }
        }

        auditLogRepository.save(new AuditLog(userId, "SYNC", "SyncEngine", null, "Delta push processed"));
        return now;
    }

    @Transactional(readOnly = true)
    public PullResponse pullDelta(Long userId, Instant since) {
        User user = getUser(userId);
        PullResponse response = new PullResponse();
        response.setServerTimestamp(Instant.now());

        Instant cutoff = since != null ? since : Instant.EPOCH;
        List<Expense> updatedExpenses = expenseRepository.findModifiedSince(user, cutoff);

        List<ExpenseDto> dtos = updatedExpenses.stream()
                .map(expenseService::mapToDto)
                .collect(Collectors.toList());

        response.setExpenses(dtos);
        return response;
    }
}
