package com.pocketmoney.repository;

import com.pocketmoney.entity.DojoAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DojoAttendanceRepository extends JpaRepository<DojoAttendance, Long> {

    List<DojoAttendance> findByUserNameOrderByDateAsc(String userName);

    Optional<DojoAttendance> findByUserNameAndDate(String userName, LocalDate date);

    void deleteByUserNameAndDate(String userName, LocalDate date);
}
