package com.pocketmoney.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/** 도장 기술 체크 (사용자 + 기술 ID 유일) */
@Entity
@Table(name = "dojo_skill",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_name", "skill_id"}))
public class DojoSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;

    /** 기술 ID (예: jab, middle-kick, closed-guard) */
    @NotBlank
    @Column(name = "skill_id", nullable = false, length = 40)
    private String skillId;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @PrePersist
    protected void onCreate() {
        if (checkedAt == null) checkedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
}
