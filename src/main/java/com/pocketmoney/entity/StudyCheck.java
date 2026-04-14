package com.pocketmoney.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 공부 완료 체크 기록 (사용자 + 날짜 + 과목 유일) */
@Entity
@Table(name = "study_check",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_name", "check_date", "subject"}))
public class StudyCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 이름 */
    @NotBlank
    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;

    /** 체크한 날짜 */
    @NotNull
    @Column(name = "check_date", nullable = false)
    private LocalDate date;

    /** 과목명 */
    @NotBlank
    @Column(nullable = false, length = 50)
    private String subject;

    /** 체크 시각 */
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    /** 공부한 시간 (분) */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @PrePersist
    protected void onCreate() {
        if (completedAt == null) completedAt = LocalDateTime.now();
    }

    // === Getters & Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}
