package com.pocketmoney.repository;

import com.pocketmoney.entity.DojoJournal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DojoJournalRepository extends JpaRepository<DojoJournal, Long> {

    List<DojoJournal> findAllByOrderByDateDescIdDesc();

    List<DojoJournal> findByUserNameOrderByDateDescIdDesc(String userName);
}
