package com.spendsmart.income.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String type;
    private Double budgetLimit;
    private Boolean isDefault;
}
