package com.camuel.sanchay.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_user", columnList = "user_id, name")
})
public class Category extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // null for system default categories

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "icon_name", nullable = false, length = 64)
    private String iconName;

    @Column(name = "is_custom")
    private boolean custom = false;

    public Category() {}

    public Category(User user, String name, String iconName, boolean custom) {
        this.user = user;
        this.name = name;
        this.iconName = iconName;
        this.custom = custom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }
}
