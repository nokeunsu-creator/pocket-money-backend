package com.pocketmoney.service;

import com.pocketmoney.entity.ReadBook;
import com.pocketmoney.entity.StudyCheck;
import com.pocketmoney.entity.StudySchedule;
import com.pocketmoney.repository.ReadBookRepository;
import com.pocketmoney.repository.StudyCheckRepository;
import com.pocketmoney.repository.StudyScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class StudyService {

    private final StudyScheduleRepository scheduleRepo;
    private final StudyCheckRepository checkRepo;
    private final ReadBookRepository bookRepo;

    public StudyService(StudyScheduleRepository scheduleRepo,
                        StudyCheckRepository checkRepo,
                        ReadBookRepository bookRepo) {
        this.scheduleRepo = scheduleRepo;
        this.checkRepo = checkRepo;
        this.bookRepo = bookRepo;
    }

    // ========== 스케줄 (요일별 과목) ==========

    /** 사용자의 전체 주간 스케줄 조회 (7개 요일) */
    public List<StudySchedule> getSchedule(String userName) {
        List<StudySchedule> existing = scheduleRepo.findByUserNameOrderByDayOfWeek(userName);
        // 7개 요일 모두 없으면 기본값으로 채워서 저장
        if (existing.size() < 7) {
            Map<DayOfWeek, StudySchedule> map = new EnumMap<>(DayOfWeek.class);
            for (StudySchedule s : existing) map.put(s.getDayOfWeek(), s);
            for (DayOfWeek dow : DayOfWeek.values()) {
                if (!map.containsKey(dow)) {
                    StudySchedule s = new StudySchedule();
                    s.setUserName(userName);
                    s.setDayOfWeek(dow);
                    s.setSubjects("수학,영어,독서");
                    map.put(dow, s);
                }
            }
            existing = new ArrayList<>(map.values());
            existing.sort(Comparator.comparing(StudySchedule::getDayOfWeek));
            // 기본값 저장 (upsert)
            saveDefaultIfMissing(userName, existing);
        }
        return existing;
    }

    @Transactional
    protected void saveDefaultIfMissing(String userName, List<StudySchedule> list) {
        for (StudySchedule s : list) {
            if (s.getId() == null) {
                scheduleRepo.save(s);
            }
        }
    }

    /** 특정 요일의 과목 수정 */
    @Transactional
    public StudySchedule updateSchedule(String userName, DayOfWeek dayOfWeek, String subjects) {
        StudySchedule s = scheduleRepo.findByUserNameAndDayOfWeek(userName, dayOfWeek)
                .orElseGet(() -> {
                    StudySchedule ns = new StudySchedule();
                    ns.setUserName(userName);
                    ns.setDayOfWeek(dayOfWeek);
                    return ns;
                });
        s.setSubjects(subjects == null ? "" : subjects);
        return scheduleRepo.save(s);
    }

    // ========== 오늘의 체크리스트 ==========

    /** 특정 날짜의 과목 + 체크 상태 반환 */
    public Map<String, Object> getDayStatus(String userName, LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        StudySchedule schedule = scheduleRepo.findByUserNameAndDayOfWeek(userName, dow)
                .orElseGet(() -> {
                    StudySchedule s = new StudySchedule();
                    s.setUserName(userName);
                    s.setDayOfWeek(dow);
                    s.setSubjects("수학,영어,독서");
                    return scheduleRepo.save(s);
                });

        List<String> subjects = parseSubjects(schedule.getSubjects());
        List<StudyCheck> checks = checkRepo.findByUserNameAndDate(userName, date);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date);
        result.put("dayOfWeek", dow.name());
        result.put("subjects", subjects);
        result.put("checks", checks);
        return result;
    }

    private List<String> parseSubjects(String subjects) {
        if (subjects == null || subjects.isBlank()) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (String s : subjects.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) list.add(t);
        }
        return list;
    }

    // ========== 체크 기록 ==========

    /** 체크 (또는 시간 업데이트) */
    @Transactional
    public StudyCheck check(String userName, LocalDate date, String subject, Integer durationMinutes) {
        StudyCheck existing = checkRepo.findByUserNameAndDateAndSubject(userName, date, subject).orElse(null);
        if (existing != null) {
            if (durationMinutes != null) existing.setDurationMinutes(durationMinutes);
            return checkRepo.save(existing);
        }
        StudyCheck c = new StudyCheck();
        c.setUserName(userName);
        c.setDate(date);
        c.setSubject(subject);
        c.setDurationMinutes(durationMinutes);
        return checkRepo.save(c);
    }

    /** 체크 해제 */
    @Transactional
    public void uncheck(String userName, LocalDate date, String subject) {
        checkRepo.findByUserNameAndDateAndSubject(userName, date, subject)
                .ifPresent(checkRepo::delete);
    }

    /** 기간 체크 히스토리 */
    public List<StudyCheck> getHistory(String userName, LocalDate start, LocalDate end) {
        return checkRepo.findByUserNameAndDateBetweenOrderByDateDescCompletedAtDesc(userName, start, end);
    }

    // ========== 읽은 책 ==========

    public List<ReadBook> getBooks(String userName) {
        return bookRepo.findByUserNameOrderByReadDateDescCreatedAtDesc(userName);
    }

    @Transactional
    public ReadBook addBook(ReadBook book) {
        if (book.getReadDate() == null) book.setReadDate(LocalDate.now());
        return bookRepo.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        bookRepo.deleteById(id);
    }
}
