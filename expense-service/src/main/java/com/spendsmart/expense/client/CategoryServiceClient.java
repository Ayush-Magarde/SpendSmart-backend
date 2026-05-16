package com.spendsmart.expense.client;

import com.spendsmart.expense.dto.CategoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "category-service")
public interface CategoryServiceClient {

    @GetMapping("/api/categories")
    List<CategoryDTO> getCategories(@RequestHeader("X-User-Id") Long userId, @RequestHeader("X-User-Email") String email);

    @GetMapping("/api/categories/type/{type}")
    List<CategoryDTO> getCategoriesByType(@RequestHeader("X-User-Id") Long userId, @PathVariable("type") String type);
}
