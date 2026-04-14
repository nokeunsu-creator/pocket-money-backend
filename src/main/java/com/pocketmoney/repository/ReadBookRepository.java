package com.pocketmoney.repository;

import com.pocketmoney.entity.ReadBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadBookRepository extends JpaRepository<ReadBook, Long> {

    List<ReadBook> findByUserNameOrderByReadDateDescCreatedAtDesc(String userName);
}
