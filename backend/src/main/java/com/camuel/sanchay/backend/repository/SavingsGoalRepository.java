package com.camuel.sanchay.backend.repository;

import com.camuel.sanchay.backend.entity.SavingsGoal;
import com.camuel.sanchay.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByUser(User user);
}
