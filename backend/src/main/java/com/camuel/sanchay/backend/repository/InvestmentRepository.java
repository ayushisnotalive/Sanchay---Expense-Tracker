package com.camuel.sanchay.backend.repository;

import com.camuel.sanchay.backend.entity.Investment;
import com.camuel.sanchay.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    List<Investment> findByUser(User user);
    List<Investment> findByUserAndSymbol(User user, String symbol);
}
