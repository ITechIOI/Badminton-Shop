package com.example.notificationservice.modules.feign.Subscriptions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubscriptionResponse(
        String endpoint,
        String auth,
        String p256dh,
        Long userId
) {
    public String toString () {
        return "SubscriptionResponse{" +
                "endpoint='" + endpoint + '\'' +
                ", auth='" + auth + '\'' +
                ", p256dh='" + p256dh + '\'' +
                ", userId=" + userId +
                '}';
    }
}
