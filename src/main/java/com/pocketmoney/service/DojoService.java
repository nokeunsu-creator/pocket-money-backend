package com.pocketmoney.service;

import com.pocketmoney.entity.DojoAttendance;
import com.pocketmoney.entity.DojoJournal;
import com.pocketmoney.entity.DojoSkill;
import com.pocketmoney.repository.DojoAttendanceRepository;
import com.pocketmoney.repository.DojoJournalRepository;
import com.pocketmoney.repository.DojoSkillRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DojoService {

    private final DojoAttendanceRepository attendanceRepo;
    private final DojoSkillRepository skillRepo;
    private final DojoJournalRepository journalRepo;

    public DojoService(DojoAttendanceRepository a, DojoSkillRepository s, DojoJournalRepository j) {
        this.attendanceRepo = a;
        this.skillRepo = s;
        this.journalRepo = j;
    }

    // ====== Attendance ======

    public List<DojoAttendance> getAttendance(String userName) {
        return attendanceRepo.findByUserNameOrderByDateAsc(userName);
    }

    @Transactional
    public DojoAttendance addAttendance(String userName, LocalDate date) {
        return attendanceRepo.findByUserNameAndDate(userName, date)
                .orElseGet(() -> {
                    DojoAttendance a = new DojoAttendance();
                    a.setUserName(userName);
                    a.setDate(date);
                    try {
                        return attendanceRepo.save(a);
                    } catch (DataIntegrityViolationException e) {
                        return attendanceRepo.findByUserNameAndDate(userName, date).orElseThrow();
                    }
                });
    }

    @Transactional
    public void removeAttendance(String userName, LocalDate date) {
        attendanceRepo.deleteByUserNameAndDate(userName, date);
    }

    // ====== Skills ======

    public List<DojoSkill> getSkills(String userName) {
        return skillRepo.findByUserName(userName);
    }

    @Transactional
    public DojoSkill addSkill(String userName, String skillId) {
        return skillRepo.findByUserNameAndSkillId(userName, skillId)
                .orElseGet(() -> {
                    DojoSkill s = new DojoSkill();
                    s.setUserName(userName);
                    s.setSkillId(skillId);
                    try {
                        return skillRepo.save(s);
                    } catch (DataIntegrityViolationException e) {
                        return skillRepo.findByUserNameAndSkillId(userName, skillId).orElseThrow();
                    }
                });
    }

    @Transactional
    public void removeSkill(String userName, String skillId) {
        skillRepo.deleteByUserNameAndSkillId(userName, skillId);
    }

    // ====== Journal ======

    public List<DojoJournal> getJournal() {
        return journalRepo.findAllByOrderByDateDescIdDesc();
    }

    public List<DojoJournal> getJournalByUser(String userName) {
        return journalRepo.findByUserNameOrderByDateDescIdDesc(userName);
    }

    @Transactional
    public DojoJournal addJournal(String userName, LocalDate date, String text) {
        DojoJournal j = new DojoJournal();
        j.setUserName(userName);
        j.setDate(date);
        j.setText(text);
        return journalRepo.save(j);
    }

    @Transactional
    public void deleteJournal(Long id) {
        journalRepo.deleteById(id);
    }
}
