package com.spendsmart.category.dto;

import com.spendsmart.category.entity.CategoryType;
import lombok.Data;

@Data
public class CategoryRequest {

    private String name;

    private String type;

    private Double budgetLimit;
    
    // Helper method to convert string to enum
    public CategoryType getCategoryTypeEnum() {
        return type != null ? CategoryType.valueOf(type.toUpperCase()) : CategoryType.EXPENSE;
    }
}