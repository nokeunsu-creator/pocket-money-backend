package com.pocketmoney.repository;

import com.pocketmoney.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findAllByOrderByStartDateAsc();
}
