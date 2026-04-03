package com.pocketmoney.controller;

import com.pocketmoney.entity.Entry;
import com.pocketmoney.service.EntryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EntryController {

    private final EntryService entryService;

    public EntryController(EntryService entryService) {
        this.entryService = entryService;
    }

    /**
     * 기록 추가
     * POST /api/entries
     */
    @PostMapping("/entries")
    public ResponseEntity<Entry> createEntry(@Valid @RequestBody Entry entry) {
        Entry saved = entryService.createEntry(entry);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * 기록 수정
     * PUT /api/entries/{id}
     */
    @PutMapping("/entries/{id}")
    public ResponseEntity<Entry> updateEntry(@PathVariable Long id, @Valid @RequestBody Entry entry) {
        Entry updated = entryService.updateEntry(id, entry);
        return ResponseEntity.ok(updated);
    }

    /**
     * 기록 삭제
     * DELETE /api/entries/{id}
     */
    @DeleteMapping("/entries/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        entryService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 기록 목록 조회
     * GET /api/entries?user=건우&year=2026&month=4
     * year, month 생략 시 전체 조회
     */
    @GetMapping("/entries")
    public ResponseEntity<List<Entry>> getEntries(
            @RequestParam String user,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        List<Entry> entries;
        if (year != null && month != null) {
            entries = entryService.getEntriesByMonth(user, year, month);
        } else {
            entries = entryService.getAllEntries(user);
        }
        return ResponseEntity.ok(entries);
    }

    /**
     * 월별 통계
     * GET /api/stats?user=건우&year=2026&month=4
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam String user,
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> stats = entryService.getMonthlyStats(user, year, month);
        return ResponseEntity.ok(stats);
    }

    /**
     * 서버 상태 확인 (Health Check)
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
