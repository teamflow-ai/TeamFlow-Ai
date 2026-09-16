package com.teamflow.ai.ai.messaging;

import com.teamflow.ai.ai.service.NotificationService;
import com.teamflow.ai.common.constant.MessagingConstants;
import com.teamflow.ai.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consumes every {@code NotificationEvent} published anywhere on the platform. */
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = MessagingConstants.QUEUE_NOTIFICATION_EVENTS)
    public void onNotificationEvent(NotificationEvent event) {
        notificationService.receive(event);
    }
}
