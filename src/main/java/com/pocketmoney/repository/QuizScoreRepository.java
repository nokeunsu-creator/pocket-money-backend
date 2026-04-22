package com.pocketmoney.repository;

import com.pocketmoney.entity.QuizScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizScoreRepository extends JpaRepository<QuizScore, Long> {

    /** 특정 월의 모든 점수 — 리더보드용 */
    List<QuizScore> findByYearMonthOrderByScoreDescPlayedAtAsc(String yearMonth);

    /** 사용자별·퀴즈별 최고 기록 조회 (월 필터) */
    @Query("SELECT q FROM QuizScore q WHERE q.yearMonth = :yearMonth " +
           "AND q.score = (SELECT MAX(q2.score) FROM QuizScore q2 " +
           "WHERE q2.userName = q.userName AND q2.quizId = q.quizId AND q2.yearMonth = :yearMonth) " +
           "ORDER BY q.quizId, q.score DESC, q.playedAt ASC")
    List<QuizScore> findMonthlyBestByUserAndQuiz(@Param("yearMonth") String yearMonth);
}
