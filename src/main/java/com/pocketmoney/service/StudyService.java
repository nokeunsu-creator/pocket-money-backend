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

    /** 사용자의 전체 주간 스케줄 조회 (7개 요일, 없으면 기본값 생성) */
    @Transactional
    public List<StudySchedule> getSchedule(String userName) {
        List<StudySchedule> existing = scheduleRepo.findByUserNameOrderByDayOfWeek(userName);
        if (existing.size() >= 7) return existing;

        Map<DayOfWeek, StudySchedule> map = new EnumMap<>(DayOfWeek.class);
        for (StudySchedule s : existing) map.put(s.getDayOfWeek(), s);
        for (DayOfWeek dow : DayOfWeek.values()) {
            if (!map.containsKey(dow)) {
                StudySchedule s = new StudySchedule();
                s.setUserName(userName);
                s.setDayOfWeek(dow);
                s.setSubjects("수학,영어,독서");
                map.put(dow, scheduleRepo.save(s));
            }
        }
        List<StudySchedule> result = new ArrayList<>(map.values());
        result.sort(Comparator.comparing(StudySchedule::getDayOfWeek));
        return result;
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
    @Transactional
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

    // ========== 연속 공부일 (streak) ==========

    /**
     * 현재 streak: 오늘(또는 어제) 기준으로 연속된 공부일 수
     * 최장 streak: 지금까지의 최장 연속 공부일
     * 획득 배지: [3,7,14,30,50,100,200,365] 중 longest 이하인 마일스톤
     */
    public Map<String, Object> getStreak(String userName) {
        List<LocalDate> dates = checkRepo.findDistinctDatesByUserNameDesc(userName);
        Map<String, Object> result = new LinkedHashMap<>();
        if (dates.isEmpty()) {
            result.put("current", 0);
            result.put("longest", 0);
            result.put("lastStudyDate", null);
            result.put("badges", List.of());
            return result;
        }

        Set<LocalDate> dateSet = new HashSet<>(dates);

        // 현재 streak
        LocalDate today = LocalDate.now();
        LocalDate cursor = dateSet.contains(today) ? today : today.minusDays(1);
        int current = 0;
        if (dateSet.contains(cursor)) {
            while (dateSet.contains(cursor)) {
                current++;
                cursor = cursor.minusDays(1);
            }
        }

        // 최장 streak (날짜 배열을 오름차순으로 정렬해 연속 구간 계산)
        List<LocalDate> ascending = new ArrayList<>(dateSet);
        Collections.sort(ascending);
        int longest = 1;
        int run = 1;
        for (int i = 1; i < ascending.size(); i++) {
            if (ascending.get(i - 1).plusDays(1).equals(ascending.get(i))) {
                run++;
                longest = Math.max(longest, run);
            } else {
                run = 1;
            }
        }

        int[] milestones = {3, 7, 14, 30, 50, 100, 200, 365};
        List<Integer> badges = new ArrayList<>();
        for (int m : milestones) if (longest >= m) badges.add(m);

        result.put("current", current);
        result.put("longest", longest);
        result.put("lastStudyDate", dates.get(0));
        result.put("badges", badges);
        return result;
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
