package com.camuel.sanchay.backend.repository;

import com.camuel.sanchay.backend.entity.Debt;
import com.camuel.sanchay.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByUserOrderByIsSettledAscIdDesc(User user);
    List<Debt> findByUserAndType(User user, String type);
}
