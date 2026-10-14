package com.camuel.sanchay.backend.repository;

import com.camuel.sanchay.backend.entity.Subscription;
import com.camuel.sanchay.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUser(User user);
    List<Subscription> findByUserOrderByNextDueDateAsc(User user);
}
