package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.SavingsGoalDto;
import com.camuel.sanchay.backend.entity.SavingsGoal;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.exception.ResourceNotFoundException;
import com.camuel.sanchay.backend.repository.SavingsGoalRepository;
import com.camuel.sanchay.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository, UserRepository userRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.userRepository = userRepository;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    public List<SavingsGoalDto> getSavingsGoals(Long userId) {
        User user = getUser(userId);
        return savingsGoalRepository.findByUser(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SavingsGoalDto createSavingsGoal(Long userId, SavingsGoalDto dto) {
        User user = getUser(userId);
        SavingsGoal goal = new SavingsGoal();
        goal.setUser(user);
        goal.setName(dto.getName());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setCurrentAmount(dto.getCurrentAmount() != null ? dto.getCurrentAmount() : 0.0f);
        goal.setDeadlineDate(dto.getDeadlineDate());

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return mapToDto(saved);
    }

    @Transactional
    public SavingsGoalDto updateSavingsGoal(Long userId, Long id, SavingsGoalDto dto) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));

        goal.setName(dto.getName());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setCurrentAmount(dto.getCurrentAmount());
        goal.setDeadlineDate(dto.getDeadlineDate());

        SavingsGoal updated = savingsGoalRepository.save(goal);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteSavingsGoal(Long userId, Long id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));

        savingsGoalRepository.delete(goal);
    }

    public SavingsGoalDto mapToDto(SavingsGoal g) {
        SavingsGoalDto dto = new SavingsGoalDto();
        dto.setId(g.getId());
        dto.setName(g.getName());
        dto.setTargetAmount(g.getTargetAmount());
        dto.setCurrentAmount(g.getCurrentAmount());
        dto.setDeadlineDate(g.getDeadlineDate());
        return dto;
    }
}
