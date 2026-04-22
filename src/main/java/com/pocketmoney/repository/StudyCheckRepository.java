package com.pocketmoney.repository;

import com.pocketmoney.entity.StudyCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyCheckRepository extends JpaRepository<StudyCheck, Long> {

    List<StudyCheck> findByUserNameAndDate(String userName, LocalDate date);

    Optional<StudyCheck> findByUserNameAndDateAndSubject(String userName, LocalDate date, String subject);

    List<StudyCheck> findByUserNameAndDateBetweenOrderByDateDescCompletedAtDesc(
            String userName, LocalDate startDate, LocalDate endDate);

    @org.springframework.data.jpa.repository.Query(
            "SELECT DISTINCT c.date FROM StudyCheck c WHERE c.userName = :userName ORDER BY c.date DESC")
    List<LocalDate> findDistinctDatesByUserNameDesc(@org.springframework.data.repository.query.Param("userName") String userName);
}
