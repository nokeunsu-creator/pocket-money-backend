package com.pocketmoney.controller;

import com.pocketmoney.entity.PushSubscription;
import com.pocketmoney.entity.Todo;
import com.pocketmoney.repository.PushSubscriptionRepository;
import com.pocketmoney.repository.TodoRepository;
import com.pocketmoney.service.PushNotificationService;
import com.pocketmoney.service.VapidService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private final VapidService vapidService;
    private final PushSubscriptionRepository subRepo;
    private final PushNotificationService pushService;
    private final TodoRepository todoRepo;

    public PushController(VapidService vapidService,
                          PushSubscriptionRepository subRepo,
                          PushNotificationService pushService,
                          TodoRepository todoRepo) {
        this.vapidService = vapidService;
        this.subRepo = subRepo;
        this.pushService = pushService;
        this.todoRepo = todoRepo;
    }

    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        return Map.of("publicKey", vapidService.getPublicKey());
    }

    @PostMapping("/subscribe")
    @Transactional
    public Map<String, Object> subscribe(@RequestBody SubscribeRequest req) {
        // 같은 endpoint 이미 있으면 업데이트, 없으면 신규
        PushSubscription sub = subRepo.findByEndpoint(req.endpoint).orElseGet(PushSubscription::new);
        sub.setUserName(req.userName);
        sub.setEndpoint(req.endpoint);
        sub.setP256dh(req.p256dh);
        sub.setAuth(req.auth);
        subRepo.save(sub);
        return Map.of("ok", true, "id", sub.getId());
    }

    @PostMapping("/unsubscribe")
    @Transactional
    public Map<String, Object> unsubscribe(@RequestBody Map<String, String> body) {
        String endpoint = body.get("endpoint");
        if (endpoint != null) subRepo.deleteByEndpoint(endpoint);
        return Map.of("ok", true);
    }

    /** 테스트용: 즉시 한 번 "오늘 할일" 알림을 모든 구독에 발송 */
    @PostMapping("/trigger-todo-reminder")
    public Map<String, Object> triggerTodoReminder() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<Todo> todos = todoRepo.findUncompletedForToday(today);
        if (todos.isEmpty()) {
            return Map.of("ok", true, "sent", 0, "message", "오늘 할 일이 없어요");
        }
        String title = "📋 오늘 할 일 " + todos.size() + "개 있어요";
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < Math.min(3, todos.size()); i++) {
            if (i > 0) body.append(", ");
            body.append(todos.get(i).getTitle());
        }
        if (todos.size() > 3) body.append(" 외 ").append(todos.size() - 3).append("개");
        int sent = pushService.sendToAll(title, body.toString(), "/");
        return Map.of("ok", true, "sent", sent);
    }

    /** 현재 구독 수 (디버그) */
    @GetMapping("/subscriptions/count")
    public Map<String, Long> count() {
        return Map.of("count", subRepo.count());
    }

    public static class SubscribeRequest {
        public String userName;
        public String endpoint;
        public String p256dh;
        public String auth;
    }
}
