package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.DebtDto;
import com.camuel.sanchay.backend.entity.Debt;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.exception.ResourceNotFoundException;
import com.camuel.sanchay.backend.repository.DebtRepository;
import com.camuel.sanchay.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DebtService {

    private final DebtRepository debtRepository;
    private final UserRepository userRepository;

    public DebtService(DebtRepository debtRepository, UserRepository userRepository) {
        this.debtRepository = debtRepository;
        this.userRepository = userRepository;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    public List<DebtDto> getAllDebts(Long userId) {
        User user = getUser(userId);
        return debtRepository.findByUserOrderByIsSettledAscIdDesc(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DebtDto createDebt(Long userId, DebtDto dto) {
        User user = getUser(userId);
        Debt debt = new Debt();
        debt.setUser(user);
        debt.setPersonName(dto.getPersonName());
        debt.setAmount(dto.getAmount());
        debt.setType(dto.getType());
        debt.setDueDate(dto.getDueDate());
        debt.setSettled(dto.isSettled());

        Debt saved = debtRepository.save(debt);
        return mapToDto(saved);
    }

    @Transactional
    public DebtDto toggleSettled(Long userId, Long debtId) {
        Debt debt = debtRepository.findById(debtId)
                .filter(d -> d.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Debt record not found"));

        debt.setSettled(!debt.isSettled());
        Debt updated = debtRepository.save(debt);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteDebt(Long userId, Long debtId) {
        Debt debt = debtRepository.findById(debtId)
                .filter(d -> d.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Debt record not found"));

        debtRepository.delete(debt);
    }

    public DebtDto mapToDto(Debt d) {
        DebtDto dto = new DebtDto();
        dto.setId(d.getId());
        dto.setPersonName(d.getPersonName());
        dto.setAmount(d.getAmount());
        dto.setType(d.getType());
        dto.setDueDate(d.getDueDate());
        dto.setSettled(d.isSettled());
        return dto;
    }
}
