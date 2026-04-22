package com.pocketmoney.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "savings_goals")
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 20)
    private String emoji;

    @NotNull
    @Column(name = "target_amount", nullable = false)
    private Integer targetAmount;

    @Column(name = "current_amount", nullable = false)
    private Integer currentAmount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (currentAmount == null) currentAmount = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public Integer getTargetAmount() { return targetAmount; }
    public void setTargetAmount(Integer targetAmount) { this.targetAmount = targetAmount; }
    public Integer getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(Integer currentAmount) { this.currentAmount = currentAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
