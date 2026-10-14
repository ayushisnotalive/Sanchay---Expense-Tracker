package com.camuel.sanchay.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SavingsGoalDto {
    private Long id;

    @NotBlank(message = "Goal name is required")
    private String name;

    @NotNull(message = "Target amount is required")
    private Float targetAmount;

    private Float currentAmount = 0.0f;

    @NotBlank(message = "Deadline date is required")
    private String deadlineDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Float getTargetAmount() { return targetAmount; }
    public void setTargetAmount(Float targetAmount) { this.targetAmount = targetAmount; }
    public Float getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(Float currentAmount) { this.currentAmount = currentAmount; }
    public String getDeadlineDate() { return deadlineDate; }
    public void setDeadlineDate(String deadlineDate) { this.deadlineDate = deadlineDate; }
}
