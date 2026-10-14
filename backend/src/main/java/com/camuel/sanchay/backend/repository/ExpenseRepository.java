package com.camuel.sanchay.backend.repository;

import com.camuel.sanchay.backend.entity.Expense;
import com.camuel.sanchay.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserAndDeletedFalseOrderByDateDesc(User user);

    Page<Expense> findByUserAndDeletedFalse(User user, Pageable pageable);

    List<Expense> findByUserAndCategoryAndDeletedFalse(User user, String category);

    List<Expense> findByUserAndWalletIdAndDeletedFalse(User user, Long walletId);

    Optional<Expense> findByClientUuidAndUser(String clientUuid, User user);

    @Query("SELECT e FROM Expense e WHERE e.user = :user AND e.updatedAt > :since")
    List<Expense> findModifiedSince(@Param("user") User user, @Param("since") Instant since);

    @Query("SELECT e.category, SUM(CAST(e.amount AS double)) FROM Expense e WHERE e.user = :user AND e.deleted = false GROUP BY e.category")
    List<Object[]> sumAmountByCategory(@Param("user") User user);
}
