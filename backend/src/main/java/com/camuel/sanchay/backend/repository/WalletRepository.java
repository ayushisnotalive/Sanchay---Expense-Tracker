package com.camuel.sanchay.backend.repository;

import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUser(User user);
    Optional<Wallet> findByIdAndUser(Long id, User user);
}
