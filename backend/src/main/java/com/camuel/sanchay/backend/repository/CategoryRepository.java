package com.camuel.sanchay.backend.repository;

import com.camuel.sanchay.backend.entity.Category;
import com.camuel.sanchay.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.user = :user OR c.user IS NULL")
    List<Category> findAllAvailableForUser(@Param("user") User user);

    List<Category> findByUser(User user);
}
