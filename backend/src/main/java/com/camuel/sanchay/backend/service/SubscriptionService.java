package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.SubscriptionDto;
import com.camuel.sanchay.backend.entity.Subscription;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.exception.ResourceNotFoundException;
import com.camuel.sanchay.backend.repository.SubscriptionRepository;
import com.camuel.sanchay.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    public List<SubscriptionDto> getSubscriptions(Long userId) {
        User user = getUser(userId);
        return subscriptionRepository.findByUserOrderByNextDueDateAsc(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionDto createSubscription(Long userId, SubscriptionDto dto) {
        User user = getUser(userId);
        Subscription s = new Subscription();
        s.setUser(user);
        s.setName(dto.getName());
        s.setAmount(dto.getAmount());
        s.setNextDueDate(dto.getNextDueDate());
        s.setFrequency(dto.getFrequency());
        s.setCategory(dto.getCategory());
        s.setWalletId(dto.getWalletId() != null ? dto.getWalletId() : 1L);
        s.setLastAutoPostDate(dto.getLastAutoPostDate());

        Subscription saved = subscriptionRepository.save(s);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteSubscription(Long userId, Long id) {
        Subscription s = subscriptionRepository.findById(id)
                .filter(sub -> sub.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        subscriptionRepository.delete(s);
    }

    public SubscriptionDto mapToDto(Subscription s) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setAmount(s.getAmount());
        dto.setNextDueDate(s.getNextDueDate());
        dto.setFrequency(s.getFrequency());
        dto.setCategory(s.getCategory());
        dto.setWalletId(s.getWalletId());
        dto.setLastAutoPostDate(s.getLastAutoPostDate());
        return dto;
    }
}
