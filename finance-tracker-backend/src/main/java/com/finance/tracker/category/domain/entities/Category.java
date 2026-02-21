package com.finance.tracker.category.domain.entities;

import com.finance.tracker.category.domain.CategoryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Automatically filter out soft-deleted items in standard queries
@Where(clause = "deleted_at IS NULL")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoryType type; // Consider using an Enum here

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "is_active")
    private boolean isActive = true;

    // --- RECURSIVE RELATIONSHIP ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    // --- AUDIT FIELDS ---

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // --- Icon related details ---
    @Column(name = "icon_key")
    private String iconKey;

    @Column(name = "color_code")
    private String colorCode;
    
    // Helper method to add children safely
    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
    }
}