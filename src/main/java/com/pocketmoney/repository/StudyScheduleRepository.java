package com.pocketmoney.repository;

import com.pocketmoney.entity.StudySchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface StudyScheduleRepository extends JpaRepository<StudySchedule, Long> {

    List<StudySchedule> findByUserNameOrderByDayOfWeek(String userName);

    Optional<StudySchedule> findByUserNameAndDayOfWeek(String userName, DayOfWeek dayOfWeek);
}
