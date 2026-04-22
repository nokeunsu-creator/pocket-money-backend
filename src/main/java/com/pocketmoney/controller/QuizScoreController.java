package com.pocketmoney.controller;

import com.pocketmoney.entity.QuizScore;
import com.pocketmoney.repository.QuizScoreRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/quiz-scores")
public class QuizScoreController {

    private final QuizScoreRepository repo;

    public QuizScoreController(QuizScoreRepository repo) {
        this.repo = repo;
    }

    /** 점수 기록 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizScore create(@Valid @RequestBody QuizScore score) {
        if (score.getYearMonth() == null) {
            LocalDate d = LocalDate.now();
            score.setYearMonth(String.format("%04d-%02d", d.getYear(), d.getMonthValue()));
        }
        return repo.save(score);
    }

    /** 월별 리더보드: quizId별로 그룹핑 + 사용자별 최고점 */
    @GetMapping("/leaderboard")
    public Map<String, Object> leaderboard(@RequestParam String month) {
        List<QuizScore> best = repo.findMonthlyBestByUserAndQuiz(month);

        // quizId 별로 그룹핑
        Map<String, List<Map<String, Object>>> byQuiz = new LinkedHashMap<>();
        for (QuizScore s : best) {
            byQuiz.computeIfAbsent(s.getQuizId(), k -> new ArrayList<>())
                  .add(Map.of(
                      "userName", s.getUserName(),
                      "grade", s.getGrade() != null ? s.getGrade() : "",
                      "score", s.getScore(),
                      "maxScore", s.getMaxScore(),
                      "playedAt", s.getPlayedAt().toString()
                  ));
        }

        // 사용자별 총점(모든 퀴즈 합) 계산
        Map<String, Integer> totalByUser = new LinkedHashMap<>();
        for (QuizScore s : best) {
            totalByUser.merge(s.getUserName(), s.getScore(), Integer::sum);
        }
        // 내림차순 정렬
        List<Map<String, Object>> totalRanking = totalByUser.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .map(e -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("userName", e.getKey());
                m.put("totalScore", e.getValue());
                return m;
            })
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("month", month);
        result.put("totalRanking", totalRanking);
        result.put("byQuiz", byQuiz);
        return result;
    }

    /** 내 기록 (옵션: user로 필터) */
    @GetMapping
    public List<QuizScore> list(@RequestParam String month) {
        return repo.findByYearMonthOrderByScoreDescPlayedAtAsc(month);
    }
}
