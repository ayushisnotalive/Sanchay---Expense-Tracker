package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.InvestmentDto;
import com.camuel.sanchay.backend.entity.Investment;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.exception.ResourceNotFoundException;
import com.camuel.sanchay.backend.repository.InvestmentRepository;
import com.camuel.sanchay.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;

    public InvestmentService(InvestmentRepository investmentRepository, UserRepository userRepository) {
        this.investmentRepository = investmentRepository;
        this.userRepository = userRepository;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    public List<InvestmentDto> getInvestments(Long userId) {
        User user = getUser(userId);
        return investmentRepository.findByUser(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvestmentDto createInvestment(Long userId, InvestmentDto dto) {
        User user = getUser(userId);
        Investment inv = new Investment();
        inv.setUser(user);
        inv.setSymbol(dto.getSymbol().toUpperCase());
        inv.setName(dto.getName());
        inv.setQuantity(dto.getQuantity());
        inv.setAverageBuyPrice(dto.getAverageBuyPrice());
        inv.setCurrentPrice(dto.getCurrentPrice());
        inv.setWalletId(dto.getWalletId() != null ? dto.getWalletId() : 1L);

        Investment saved = investmentRepository.save(inv);
        return mapToDto(saved);
    }

    @Transactional
    public InvestmentDto updateCurrentPrice(Long userId, Long id, Double newPrice) {
        Investment inv = investmentRepository.findById(id)
                .filter(i -> i.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));

        inv.setCurrentPrice(newPrice);
        Investment updated = investmentRepository.save(inv);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteInvestment(Long userId, Long id) {
        Investment inv = investmentRepository.findById(id)
                .filter(i -> i.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));

        investmentRepository.delete(inv);
    }

    public InvestmentDto mapToDto(Investment i) {
        InvestmentDto dto = new InvestmentDto();
        dto.setId(i.getId());
        dto.setSymbol(i.getSymbol());
        dto.setName(i.getName());
        dto.setQuantity(i.getQuantity());
        dto.setAverageBuyPrice(i.getAverageBuyPrice());
        dto.setCurrentPrice(i.getCurrentPrice());
        dto.setWalletId(i.getWalletId());
        return dto;
    }
}
