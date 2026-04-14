package com.pocketmoney.repository;

import com.pocketmoney.entity.GrowthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GrowthRecordRepository extends JpaRepository<GrowthRecord, Long> {
    List<GrowthRecord> findByUserNameOrderByDateAsc(String userName);
    Optional<GrowthRecord> findByUserNameAndDate(String userName, java.time.LocalDate date);
}
