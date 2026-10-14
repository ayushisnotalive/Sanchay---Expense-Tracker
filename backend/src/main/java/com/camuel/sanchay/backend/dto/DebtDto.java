package com.camuel.sanchay.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DebtDto {
    private Long id;

    @NotBlank(message = "Person name is required")
    private String personName;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotBlank(message = "Debt type is required ('Lent' or 'Borrowed')")
    private String type;

    @NotBlank(message = "Due date is required")
    private String dueDate;

    private boolean settled = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public boolean isSettled() { return settled; }
    public void setSettled(boolean settled) { this.settled = settled; }
}
