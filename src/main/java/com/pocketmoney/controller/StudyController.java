package com.pocketmoney.controller;

import com.pocketmoney.entity.ReadBook;
import com.pocketmoney.entity.StudyCheck;
import com.pocketmoney.entity.StudySchedule;
import com.pocketmoney.service.StudyService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    // ========== 스케줄 ==========

    /** GET /api/study/schedule?user=노건우 */
    @GetMapping("/schedule")
    public ResponseEntity<List<StudySchedule>> getSchedule(@RequestParam String user) {
        return ResponseEntity.ok(studyService.getSchedule(user));
    }

    /** PUT /api/study/schedule?user=노건우&day=MONDAY  body: {"subjects":"수학,영어,독서"} */
    @PutMapping("/schedule")
    public ResponseEntity<StudySchedule> updateSchedule(
            @RequestParam String user,
            @RequestParam DayOfWeek day,
            @RequestBody Map<String, String> body) {
        String subjects = body.getOrDefault("subjects", "");
        return ResponseEntity.ok(studyService.updateSchedule(user, day, subjects));
    }

    // ========== 오늘의 체크리스트 ==========

    /** GET /api/study/day?user=노건우&date=2026-04-14 */
    @GetMapping("/day")
    public ResponseEntity<Map<String, Object>> getDayStatus(
            @RequestParam String user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(studyService.getDayStatus(user, date));
    }

    // ========== 체크 ==========

    /** POST /api/study/check  body: {user,date,subject,durationMinutes?} */
    @PostMapping("/check")
    public ResponseEntity<StudyCheck> check(@RequestBody Map<String, Object> body) {
        String user = (String) body.get("user");
        LocalDate date = LocalDate.parse((String) body.get("date"));
        String subject = (String) body.get("subject");
        Integer duration = body.get("durationMinutes") == null ? null
                : ((Number) body.get("durationMinutes")).intValue();
        StudyCheck c = studyService.check(user, date, subject, duration);
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    /** DELETE /api/study/check?user=노건우&date=2026-04-14&subject=수학 */
    @DeleteMapping("/check")
    public ResponseEntity<Void> uncheck(
            @RequestParam String user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String subject) {
        studyService.uncheck(user, date, subject);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/study/streak?user=노건우 */
    @GetMapping("/streak")
    public ResponseEntity<Map<String, Object>> getStreak(@RequestParam String user) {
        return ResponseEntity.ok(studyService.getStreak(user));
    }

    /** GET /api/study/history?user=노건우&from=2026-04-01&to=2026-04-30 */
    @GetMapping("/history")
    public ResponseEntity<List<StudyCheck>> getHistory(
            @RequestParam String user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(studyService.getHistory(user, from, to));
    }

    // ========== 읽은 책 ==========

    /** GET /api/study/books?user=노건우 */
    @GetMapping("/books")
    public ResponseEntity<List<ReadBook>> getBooks(@RequestParam String user) {
        return ResponseEntity.ok(studyService.getBooks(user));
    }

    /** POST /api/study/books  body: {userName,title,readDate} */
    @PostMapping("/books")
    public ResponseEntity<ReadBook> addBook(@Valid @RequestBody ReadBook book) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studyService.addBook(book));
    }

    /** DELETE /api/study/books/{id} */
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        studyService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
