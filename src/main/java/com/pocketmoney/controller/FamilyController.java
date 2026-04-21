package com.pocketmoney.controller;

import com.pocketmoney.entity.*;
import com.pocketmoney.repository.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FamilyController {

    private final TodoRepository todoRepo;
    private final MemoRepository memoRepo;
    private final GrowthRecordRepository growthRepo;
    private final TimerRecordRepository timerRepo;
    private final ProfilePhotoRepository photoRepo;

    public FamilyController(TodoRepository todoRepo, MemoRepository memoRepo,
                            GrowthRecordRepository growthRepo, TimerRecordRepository timerRepo,
                            ProfilePhotoRepository photoRepo) {
        this.todoRepo = todoRepo;
        this.memoRepo = memoRepo;
        this.growthRepo = growthRepo;
        this.timerRepo = timerRepo;
        this.photoRepo = photoRepo;
    }

    // ==================== 프로필 사진 ====================

    @GetMapping("/profile/photos")
    public ResponseEntity<List<ProfilePhoto>> getAllPhotos() {
        return ResponseEntity.ok(photoRepo.findAll());
    }

    @PutMapping("/profile/photo")
    public ResponseEntity<ProfilePhoto> upsertPhoto(@RequestBody ProfilePhoto photo) {
        return ResponseEntity.ok(photoRepo.save(photo));
    }

    // ==================== 할 일 ====================

    @GetMapping("/todos")
    public ResponseEntity<List<Todo>> getTodos() {
        return ResponseEntity.ok(todoRepo.findAllByOrderByCreatedAtDesc());
    }

    @PostMapping("/todos")
    public ResponseEntity<Todo> createTodo(@Valid @RequestBody Todo todo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(todoRepo.save(todo));
    }

    @PutMapping("/todos/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id, @RequestBody Todo updated) {
        Todo todo = todoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("할 일 없음: " + id));
        if (updated.getTitle() != null) todo.setTitle(updated.getTitle());
        if (updated.getCategory() != null) todo.setCategory(updated.getCategory());
        if (updated.getDate() != null) todo.setDate(updated.getDate());
        if (updated.getImportant() != null) todo.setImportant(updated.getImportant());
        if (updated.getRepeatType() != null) todo.setRepeatType(updated.getRepeatType());
        if (updated.getCompleted() != null) {
            todo.setCompleted(updated.getCompleted());
            todo.setCompletedAt(updated.getCompleted() ? LocalDateTime.now() : null);
        }
        if (updated.getCompletedDates() != null) todo.setCompletedDates(updated.getCompletedDates());
        return ResponseEntity.ok(todoRepo.save(todo));
    }

    @DeleteMapping("/todos/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        todoRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== 메모 ====================

    @GetMapping("/memos")
    public ResponseEntity<List<Memo>> getMemos() {
        return ResponseEntity.ok(memoRepo.findAllByOrderByPinnedDescUpdatedAtDesc());
    }

    @PostMapping("/memos")
    public ResponseEntity<Memo> createMemo(@RequestBody Memo memo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memoRepo.save(memo));
    }

    @PutMapping("/memos/{id}")
    public ResponseEntity<Memo> updateMemo(@PathVariable Long id, @RequestBody Memo updated) {
        Memo memo = memoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("메모 없음: " + id));
        if (updated.getTitle() != null) memo.setTitle(updated.getTitle());
        if (updated.getContent() != null) memo.setContent(updated.getContent());
        if (updated.getColor() != null) memo.setColor(updated.getColor());
        if (updated.getPinned() != null) memo.setPinned(updated.getPinned());
        return ResponseEntity.ok(memoRepo.save(memo));
    }

    @DeleteMapping("/memos/{id}")
    public ResponseEntity<Void> deleteMemo(@PathVariable Long id) {
        memoRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== 성장 기록 ====================

    @GetMapping("/growth")
    public ResponseEntity<List<GrowthRecord>> getGrowth(@RequestParam String user) {
        return ResponseEntity.ok(growthRepo.findByUserNameOrderByDateAsc(user));
    }

    @PostMapping("/growth")
    public ResponseEntity<GrowthRecord> upsertGrowth(@Valid @RequestBody GrowthRecord record) {
        // 같은 날짜면 업데이트 (upsert)
        Optional<GrowthRecord> existing = growthRepo.findByUserNameAndDate(record.getUserName(), record.getDate());
        if (existing.isPresent()) {
            GrowthRecord e = existing.get();
            if (record.getHeight() != null) e.setHeight(record.getHeight());
            if (record.getWeight() != null) e.setWeight(record.getWeight());
            return ResponseEntity.ok(growthRepo.save(e));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(growthRepo.save(record));
    }

    @DeleteMapping("/growth/{id}")
    public ResponseEntity<Void> deleteGrowth(@PathVariable Long id) {
        growthRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== 공부 타이머 ====================

    @GetMapping("/timer/records")
    public ResponseEntity<List<TimerRecord>> getTimerRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (date != null) return ResponseEntity.ok(timerRepo.findByDateOrderByCreatedAtDesc(date));
        if (from != null && to != null) return ResponseEntity.ok(timerRepo.findByDateBetweenOrderByDateDescCreatedAtDesc(from, to));
        return ResponseEntity.ok(timerRepo.findAllByOrderByDateDescCreatedAtDesc());
    }

    @PostMapping("/timer/records")
    public ResponseEntity<TimerRecord> addTimerRecord(@Valid @RequestBody TimerRecord record) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timerRepo.save(record));
    }

    @DeleteMapping("/timer/records/{id}")
    public ResponseEntity<Void> deleteTimerRecord(@PathVariable Long id) {
        timerRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
