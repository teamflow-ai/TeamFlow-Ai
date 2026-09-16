package com.teamflow.ai.ai.service;

import com.teamflow.ai.ai.document.EmployeeProfile;
import com.teamflow.ai.ai.document.NotificationDocument;
import com.teamflow.ai.ai.dto.response.NotificationResponse;
import com.teamflow.ai.ai.mapper.NotificationMapper;
import com.teamflow.ai.ai.repository.EmployeeProfileRepository;
import com.teamflow.ai.ai.repository.NotificationRepository;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.event.NotificationEvent;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Persists and serves in-app notifications, and best-effort mirrors each one to
 * email.
 *
 * <p>Categories are deliberately not filtered here — every notification worth
 * raising in-app is also worth emailing, and a recipient who wants to mute email
 * can do so at the mail-client level. Keeping the rule this simple avoids a second
 * configuration surface nobody asked for.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;

    public void receive(NotificationEvent event) {
        NotificationDocument document = new NotificationDocument();
        document.setId(event.eventId().toString());
        document.setRecipientEmployeeId(event.recipientEmployeeId().toString());
        document.setTitle(event.title());
        document.setBody(event.body());
        document.setCategory(event.category());
        document.setTargetUrl(event.targetUrl());
        document.setRead(false);
        document.setCreatedAt(event.occurredAt() != null ? event.occurredAt() : Instant.now());
        notificationRepository.save(document);
        log.debug("Notification '{}' recorded for employee {}", event.title(), event.recipientEmployeeId());

        employeeProfileRepository.findById(event.recipientEmployeeId().toString())
                .map(EmployeeProfile::getEmail)
                .ifPresent(email -> emailService.send(email, event.title(), event.body()));
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(UUID employeeId, boolean unreadOnly, Pageable pageable) {
        Page<NotificationDocument> page = unreadOnly
                ? notificationRepository.findAllByRecipientEmployeeIdAndReadFalseOrderByCreatedAtDesc(employeeId.toString(), pageable)
                : notificationRepository.findAllByRecipientEmployeeIdOrderByCreatedAtDesc(employeeId.toString(), pageable);
        return PageResponse.from(page, notificationMapper::toResponse);
    }

    public long unreadCount(UUID employeeId) {
        return notificationRepository.countByRecipientEmployeeIdAndReadFalse(employeeId.toString());
    }

    public NotificationResponse markRead(String id, UUID employeeId) {
        NotificationDocument document = findOwned(id, employeeId);
        document.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(document));
    }

    public void markAllRead(UUID employeeId) {
        var page = notificationRepository.findAllByRecipientEmployeeIdAndReadFalseOrderByCreatedAtDesc(
                employeeId.toString(), org.springframework.data.domain.Pageable.unpaged());
        page.forEach(document -> document.setRead(true));
        notificationRepository.saveAll(page.getContent());
    }

    public void delete(String id, UUID employeeId) {
        NotificationDocument document = findOwned(id, employeeId);
        notificationRepository.delete(document);
    }

    private NotificationDocument findOwned(String id, UUID employeeId) {
        NotificationDocument document = notificationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
        if (!document.getRecipientEmployeeId().equals(employeeId.toString())) {
            throw new BusinessException("This notification does not belong to you");
        }
        return document;
    }
}
