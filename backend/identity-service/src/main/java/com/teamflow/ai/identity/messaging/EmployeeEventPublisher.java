package com.teamflow.ai.identity.messaging;

import com.teamflow.ai.common.constant.MessagingConstants;
import com.teamflow.ai.common.event.EmployeeEvent;
import com.teamflow.ai.common.event.LeaveEvent;
import com.teamflow.ai.identity.entity.Employee;
import com.teamflow.ai.identity.entity.LeaveRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes workforce domain events onto the shared topic exchange.
 *
 * <p>Consumed primarily by ai-service, which keeps its own read model of employee
 * capacity and skills so the workload scorer never needs a synchronous call back
 * into identity-service on the hot path.
 *
 * <p>Publish failures are logged rather than thrown: a lost event degrades the
 * freshness of a downstream projection, but must never fail the HTTP request that
 * triggered it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void employeeCreated(Employee employee) {
        publish(MessagingConstants.EMPLOYEE_CREATED, employee);
    }

    public void employeeUpdated(Employee employee) {
        publish(MessagingConstants.EMPLOYEE_UPDATED, employee);
    }

    public void employeeDeleted(Employee employee) {
        publish(MessagingConstants.EMPLOYEE_DELETED, employee);
    }

    public void leaveApproved(LeaveRequest leaveRequest) {
        try {
            LeaveEvent event = LeaveEvent.of(MessagingConstants.LEAVE_APPROVED, leaveRequest.getId(),
                    leaveRequest.getEmployee().getId(), leaveRequest.getStatus(),
                    leaveRequest.getStartDate(), leaveRequest.getEndDate());
            rabbitTemplate.convertAndSend(MessagingConstants.TOPIC_EXCHANGE, event.routingKey(), event);
        } catch (Exception ex) {
            log.warn("Failed to publish {} for leave request {}: {}",
                    MessagingConstants.LEAVE_APPROVED, leaveRequest.getId(), ex.getMessage());
        }
    }

    private void publish(String routingKey, Employee employee) {
        try {
            java.util.UUID primaryTeamId = null;
            java.util.Set<java.util.UUID> secondaryTeamIds = new java.util.HashSet<>();
            if (employee.getTeams() != null) {
                for (com.teamflow.ai.identity.entity.EmployeeTeamMembership tm : employee.getTeams()) {
                    if (tm.isPrimary()) {
                        primaryTeamId = tm.getTeam().getId();
                    } else {
                        secondaryTeamIds.add(tm.getTeam().getId());
                    }
                }
            }

            EmployeeEvent event = EmployeeEvent.of(routingKey, employee.getId(), employee.getWorkEmail(),
                    employee.getFullName(), employee.getDepartment() != null ? employee.getDepartment().getId() : null,
                    primaryTeamId, secondaryTeamIds,
                    employee.getSkills(), employee.getWeeklyCapacityHours());
            rabbitTemplate.convertAndSend(MessagingConstants.TOPIC_EXCHANGE, event.routingKey(), event);
        } catch (Exception ex) {
            log.warn("Failed to publish {} for employee {}: {}", routingKey, employee.getId(), ex.getMessage());
        }
    }
}
