package com.pocketmoney.repository;

import com.pocketmoney.entity.BankEntry;
import com.pocketmoney.entity.BankEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BankEntryRepository extends JpaRepository<BankEntry, Long> {

    List<BankEntry> findByUserNameAndDeletedFalseAndEntryDateBetweenOrderByEntryDateDescCreatedAtDesc(
            String userName, LocalDate startDate, LocalDate endDate);

    List<BankEntry> findByUserNameAndDeletedFalseOrderByEntryDateDescCreatedAtDesc(String userName);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM BankEntry e WHERE e.userName = :userName AND e.type = :type AND e.deleted = false")
    Integer sumAmountByUserNameAndType(@Param("userName") String userName, @Param("type") BankEntryType type);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM BankEntry e " +
           "WHERE e.userName = :userName AND e.type = :type AND e.deleted = false " +
           "AND e.entryDate BETWEEN :startDate AND :endDate")
    Integer sumAmountByUserNameAndTypeAndPeriod(
            @Param("userName") String userName,
            @Param("type") BankEntryType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<BankEntry> findByUserNameAndDeletedTrueOrderByDeletedAtDesc(String userName);
}
