package com.finance.tracker.category.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import com.finance.tracker.category.domain.CategoryType;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "_id")
    private String id;
    private String key;
    private String label;

    // if created by user than userId(user scope) otherwise null(global scope)
    private String userId;

    private String parentId;
    
    private Boolean isExpense;
    private Boolean isIncome;
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private CategoryType type;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private String metadata;

}
