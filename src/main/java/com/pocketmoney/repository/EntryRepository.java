package com.pocketmoney.repository;

import com.pocketmoney.entity.Entry;
import com.pocketmoney.entity.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    /** 특정 사용자의 기간별 기록 조회 (최신순) */
    List<Entry> findByUserNameAndEntryDateBetweenOrderByEntryDateDescCreatedAtDesc(
            String userName, LocalDate startDate, LocalDate endDate);

    /** 특정 사용자의 전체 기록 조회 (최신순) */
    List<Entry> findByUserNameOrderByEntryDateDescCreatedAtDesc(String userName);

    /** 특정 사용자 + 유형별 합계 (전체 기간) */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Entry e WHERE e.userName = :userName AND e.type = :type")
    Integer sumAmountByUserNameAndType(@Param("userName") String userName, @Param("type") EntryType type);

    /** 특정 사용자 + 기간 + 유형별 합계 */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Entry e " +
           "WHERE e.userName = :userName AND e.type = :type " +
           "AND e.entryDate BETWEEN :startDate AND :endDate")
    Integer sumAmountByUserNameAndTypeAndPeriod(
            @Param("userName") String userName,
            @Param("type") EntryType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /** 카테고리별 합계 (특정 사용자 + 기간 + 유형) */
    @Query("SELECT e.category, SUM(e.amount) FROM Entry e " +
           "WHERE e.userName = :userName AND e.type = :type " +
           "AND e.entryDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumByCategoryAndPeriod(
            @Param("userName") String userName,
            @Param("type") EntryType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
