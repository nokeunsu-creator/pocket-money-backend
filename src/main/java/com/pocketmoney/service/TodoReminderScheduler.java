package com.pocketmoney.service;

import com.pocketmoney.entity.Todo;
import com.pocketmoney.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class TodoReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(TodoReminderScheduler.class);

    private final TodoRepository todoRepo;
    private final PushNotificationService pushService;

    public TodoReminderScheduler(TodoRepository todoRepo, PushNotificationService pushService) {
        this.todoRepo = todoRepo;
        this.pushService = pushService;
    }

    /** 매일 오전 8시(한국 시간) 오늘의 미완료 할 일을 모든 등록 기기에 푸시 */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void sendMorningTodoReminder() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<Todo> todos = todoRepo.findUncompletedForToday(today);
        if (todos.isEmpty()) {
            log.info("오전 8시 리마인더: 오늘 할 일 없음, 알림 스킵");
            return;
        }
        String title = "📋 오늘 할 일 " + todos.size() + "개 있어요";
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < Math.min(3, todos.size()); i++) {
            if (i > 0) body.append(", ");
            body.append(todos.get(i).getTitle());
        }
        if (todos.size() > 3) body.append(" 외 ").append(todos.size() - 3).append("개");

        int sent = pushService.sendToAll(title, body.toString(), "/");
        log.info("오전 8시 리마인더 발송 완료: {}건", sent);
    }
}
