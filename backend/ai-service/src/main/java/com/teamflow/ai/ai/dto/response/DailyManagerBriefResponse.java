package com.teamflow.ai.ai.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record DailyManagerBriefResponse(
        List<String> todaysDeadlines,
        List<String> blockedTasks,
        List<String> overloadedEmployees,
        List<String> employeesOnLeaveToday,
        List<String> upcomingMeetings,
        List<String> atRiskProjects,
        List<String> suggestedActions,
        String summary) {
}
