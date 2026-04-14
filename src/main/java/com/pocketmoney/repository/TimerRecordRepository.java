package com.pocketmoney.repository;

import com.pocketmoney.entity.TimerRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TimerRecordRepository extends JpaRepository<TimerRecord, Long> {
    List<TimerRecord> findByDateOrderByCreatedAtDesc(LocalDate date);
    List<TimerRecord> findByDateBetweenOrderByDateDescCreatedAtDesc(LocalDate start, LocalDate end);
    List<TimerRecord> findAllByOrderByDateDescCreatedAtDesc();
}
