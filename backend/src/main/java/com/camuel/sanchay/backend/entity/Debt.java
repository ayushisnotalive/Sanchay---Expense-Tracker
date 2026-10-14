package com.camuel.sanchay.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "debts", indexes = {
    @Index(name = "idx_debt_user_settled", columnList = "user_id, is_settled")
})
public class Debt extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "person_name", nullable = false, length = 100)
    private String personName;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 20)
    private String type; // "Lent" or "Borrowed"

    @Column(name = "due_date", nullable = false, length = 32)
    private String dueDate;

    @Column(name = "is_settled", nullable = false)
    private boolean isSettled = false;

    public Debt() {}

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

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isSettled() {
        return isSettled;
    }

    public void setSettled(boolean settled) {
        isSettled = settled;
    }
}
