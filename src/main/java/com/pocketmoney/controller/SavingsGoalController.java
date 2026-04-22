package com.pocketmoney.controller;

import com.pocketmoney.entity.SavingsGoal;
import com.pocketmoney.repository.SavingsGoalRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalRepository repo;

    public SavingsGoalController(SavingsGoalRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<SavingsGoal> list(@RequestParam String user) {
        return repo.findByUserNameOrderByCompletedAtAscCreatedAtDesc(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavingsGoal create(@Valid @RequestBody SavingsGoal goal) {
        return repo.save(goal);
    }

    @PutMapping("/{id}")
    public SavingsGoal update(@PathVariable Long id, @RequestBody SavingsGoal updated) {
        SavingsGoal goal = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("저축 목표 없음: " + id));
        if (updated.getTitle() != null) goal.setTitle(updated.getTitle());
        if (updated.getEmoji() != null) goal.setEmoji(updated.getEmoji());
        if (updated.getTargetAmount() != null) goal.setTargetAmount(updated.getTargetAmount());
        if (updated.getCurrentAmount() != null) {
            goal.setCurrentAmount(updated.getCurrentAmount());
            // 목표 달성 시 completedAt 설정, 미달성으로 되돌리면 null로
            if (goal.getCurrentAmount() >= goal.getTargetAmount() && goal.getCompletedAt() == null) {
                goal.setCompletedAt(LocalDateTime.now());
            } else if (goal.getCurrentAmount() < goal.getTargetAmount()) {
                goal.setCompletedAt(null);
            }
        }
        return repo.save(goal);
    }

    /** 편의 API: 금액 증감 */
    @PostMapping("/{id}/add")
    public SavingsGoal addAmount(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        SavingsGoal goal = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("저축 목표 없음: " + id));
        int delta = body.getOrDefault("delta", 0);
        int next = Math.max(0, goal.getCurrentAmount() + delta);
        goal.setCurrentAmount(next);
        if (next >= goal.getTargetAmount() && goal.getCompletedAt() == null) {
            goal.setCompletedAt(LocalDateTime.now());
        } else if (next < goal.getTargetAmount()) {
            goal.setCompletedAt(null);
        }
        return repo.save(goal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
