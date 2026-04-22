package com.pocketmoney.service;

import com.pocketmoney.entity.PushSubscription;
import com.pocketmoney.repository.PushSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);
    private static final String VAPID_SUBJECT = "mailto:nokeunsu@gmail.com";

    private final VapidService vapidService;
    private final PushSubscriptionRepository subRepo;
    private PushService pushService;

    public PushNotificationService(VapidService vapidService, PushSubscriptionRepository subRepo) {
        this.vapidService = vapidService;
        this.subRepo = subRepo;
    }

    @PostConstruct
    public void init() throws Exception {
        pushService = new PushService();
        pushService.setPublicKey(vapidService.getPublicKey());
        pushService.setPrivateKey(vapidService.getPrivateKey());
        pushService.setSubject(VAPID_SUBJECT);
    }

    /** 모든 등록 기기에 같은 알림 전송. 만료 구독은 자동 삭제. */
    @Transactional
    public int sendToAll(String title, String body, String url) {
        List<PushSubscription> subs = subRepo.findAll();
        int sent = 0;
        for (PushSubscription sub : subs) {
            if (send(sub, title, body, url)) sent++;
        }
        return sent;
    }

    /** 단일 구독에 전송. 성공 여부 반환. 만료(410)는 구독 자동 삭제. */
    @Transactional
    public boolean send(PushSubscription sub, String title, String body, String url) {
        try {
            Subscription.Keys keys = new Subscription.Keys(sub.getP256dh(), sub.getAuth());
            Subscription subscription = new Subscription(sub.getEndpoint(), keys);

            String payload = String.format(
                "{\"title\":%s,\"body\":%s,\"url\":%s}",
                jsonString(title), jsonString(body), jsonString(url)
            );

            Notification notification = new Notification(subscription, payload);
            var res = pushService.send(notification);
            int status = res.getStatusLine().getStatusCode();

            if (status == 404 || status == 410) {
                log.info("만료된 구독 삭제: {}", sub.getEndpoint());
                subRepo.deleteByEndpoint(sub.getEndpoint());
                return false;
            }
            if (status >= 200 && status < 300) return true;

            log.warn("푸시 응답 비정상 status={}, endpoint={}", status, sub.getEndpoint());
            return false;
        } catch (Exception e) {
            log.error("푸시 전송 실패 endpoint={}, err={}", sub.getEndpoint(), e.getMessage());
            return false;
        }
    }

    private static String jsonString(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }
}
