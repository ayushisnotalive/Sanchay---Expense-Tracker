package com.camuel.sanchay.backend.repository;

import com.camuel.sanchay.backend.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
}
