package com.pocketmoney.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_scores",
       indexes = {
           @Index(name = "idx_quiz_scores_month", columnList = "year_month"),
           @Index(name = "idx_quiz_scores_lookup", columnList = "quiz_id,year_month")
       })
public class QuizScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    @NotBlank
    @Column(name = "quiz_id", nullable = false, length = 30)
    private String quizId;

    @Column(length = 20)
    private String grade;

    @NotNull
    @Column(nullable = false)
    private Integer score;

    @NotNull
    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    /** "2026-04" 형식 — 월별 집계/순위 기준 */
    @NotBlank
    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    @Column(name = "played_at", nullable = false, updatable = false)
    private LocalDateTime playedAt;

    @PrePersist
    protected void onCreate() {
        if (playedAt == null) playedAt = LocalDateTime.now();
        if (yearMonth == null) {
            LocalDate d = LocalDate.now();
            yearMonth = String.format("%04d-%02d", d.getYear(), d.getMonthValue());
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}
