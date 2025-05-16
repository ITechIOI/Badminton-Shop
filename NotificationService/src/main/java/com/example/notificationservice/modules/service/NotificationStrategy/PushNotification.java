package com.example.notificationservice.modules.service.NotificationStrategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Urgency;
import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Security;

@Service
@RequiredArgsConstructor
@Getter
@Setter
public class PushNotification implements NotificationStrategy {

    @Value("${push.vapid.public-key}")
    private String publicKey;

    @Value("${push.vapid.private-key}")
    private String privateKey;

    @Value("${push.vapid.subject}")
    private String subject;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void sendNotification(String to, String subjectText, String payloadText) {
        try {
            // Parse 'to' ra Subscription như bạn đang làm…
            Subscription subscription = buildSubscriptionFromJson(to);

            // --- Build JSON payload ---
            ObjectNode data = objectMapper.createObjectNode()
                    .put("title", subjectText)
                    .put("body" , payloadText);
            String jsonPayload = objectMapper.writeValueAsString(data);
            // --------------------------------

            Notification notification =
                    new Notification(subscription, jsonPayload, Urgency.NORMAL);

            PushService pushService = new PushService()
                    .setPublicKey ( Utils.loadPublicKey (publicKey) )
                    .setPrivateKey( Utils.loadPrivateKey(privateKey) )
                    .setSubject   ( this.subject );
            pushService.send(notification);

        } catch (Exception e) {
            System.err.println("Error sending push notification: " + e.getMessage());
        }
    }

    private Subscription buildSubscriptionFromJson(String jsonStr) throws Exception {
        JsonNode json = objectMapper.readTree(jsonStr);
        String endpoint = json.get("endpoint").asText();
        String p256dh   = json.get("keys").get("p256dh").asText();
        String auth     = json.get("keys").get("auth").asText();
        System.out.println("Endpoint: " + endpoint + ", p256dh: " + p256dh + ", auth: " + auth);
        return new Subscription(endpoint, new Subscription.Keys(p256dh, auth));
    }
}
