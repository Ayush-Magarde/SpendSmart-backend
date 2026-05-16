package com.spendsmart.category.repository;



import com.spendsmart.category.entity.Category;

import com.spendsmart.category.entity.CategoryType;

import org.springframework.data.jpa.repository.JpaRepository;



import java.util.List;

import java.util.Optional;



public interface CategoryRepository extends JpaRepository<Category, Long> {



    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdAndType(Long userId, String type);

    Optional<Category> findByUserIdAndName(Long userId, String name);

}