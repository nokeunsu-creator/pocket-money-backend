package com.pocketmoney.repository;

import com.pocketmoney.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findAllByOrderByCreatedAtDesc();

    /** 오늘 해야 할 할일: 날짜가 오늘이거나 날짜가 비어있는 미완료 항목 */
    @Query("SELECT t FROM Todo t WHERE t.completed = false AND (t.date = :today OR t.date IS NULL)")
    List<Todo> findUncompletedForToday(@Param("today") LocalDate today);
}
