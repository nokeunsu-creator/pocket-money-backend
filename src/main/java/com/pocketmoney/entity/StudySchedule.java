package com.pocketmoney.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;

/** 요일별 공부 과목 계획 (아이별 7개 row) */
@Entity
@Table(name = "study_schedule",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_name", "day_of_week"}))
public class StudySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 이름: "노건우" 또는 "노승우" */
    @NotBlank
    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;

    /** 요일: MONDAY ~ SUNDAY */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    /** 쉼표 구분 과목 목록 (예: "수학,영어,독서") */
    @Column(length = 300)
    private String subjects;

    // === Getters & Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }
}
