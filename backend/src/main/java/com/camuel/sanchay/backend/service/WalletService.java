package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.WalletDto;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.entity.Wallet;
import com.camuel.sanchay.backend.exception.ResourceNotFoundException;
import com.camuel.sanchay.backend.repository.UserRepository;
import com.camuel.sanchay.backend.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    public List<WalletDto> getWalletsForUser(Long userId) {
        User user = getUser(userId);
        return walletRepository.findByUser(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public WalletDto createWallet(Long userId, WalletDto dto) {
        User user = getUser(userId);
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setName(dto.getName());
        wallet.setBalance(dto.getBalance());
        wallet.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "$");
        wallet.setColorHex(dto.getColorHex() != null ? dto.getColorHex() : "#00F3FF");

        Wallet saved = walletRepository.save(wallet);
        return mapToDto(saved);
    }

    @Transactional
    public WalletDto updateWallet(Long userId, Long walletId, WalletDto dto) {
        Wallet wallet = walletRepository.findByIdAndUser(walletId, getUser(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));

        wallet.setName(dto.getName());
        wallet.setBalance(dto.getBalance());
        if (dto.getCurrency() != null) wallet.setCurrency(dto.getCurrency());
        if (dto.getColorHex() != null) wallet.setColorHex(dto.getColorHex());

        Wallet updated = walletRepository.save(wallet);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteWallet(Long userId, Long walletId) {
        Wallet wallet = walletRepository.findByIdAndUser(walletId, getUser(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));

        walletRepository.delete(wallet);
    }

    public WalletDto mapToDto(Wallet w) {
        WalletDto dto = new WalletDto();
        dto.setId(w.getId());
        dto.setName(w.getName());
        dto.setBalance(w.getBalance());
        dto.setCurrency(w.getCurrency());
        dto.setColorHex(w.getColorHex());
        return dto;
    }
}
