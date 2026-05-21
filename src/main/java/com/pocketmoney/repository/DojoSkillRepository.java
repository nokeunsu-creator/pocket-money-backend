package com.pocketmoney.repository;

import com.pocketmoney.entity.DojoSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DojoSkillRepository extends JpaRepository<DojoSkill, Long> {

    List<DojoSkill> findByUserName(String userName);

    Optional<DojoSkill> findByUserNameAndSkillId(String userName, String skillId);

    void deleteByUserNameAndSkillId(String userName, String skillId);
}
