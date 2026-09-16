package com.teamflow.ai.project.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.util.CsvWriter;
import com.teamflow.ai.project.dto.response.EmployeeProductivityReportResponse;
import com.teamflow.ai.project.dto.response.ProjectReportResponse;
import com.teamflow.ai.project.dto.response.SprintReportResponse;
import com.teamflow.ai.project.dto.response.WorklogReportResponse;
import com.teamflow.ai.project.service.ReportService;
import com.teamflow.ai.project.util.ExcelWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Project, Sprint, Worklog and Employee Productivity reports.
 *
 * <p>Every endpoint accepts {@code format=json} (default), {@code format=csv} or
 * {@code format=xlsx}. PDF export is not implemented — see the CDAC gap analysis
 * for the reasoning — but the seam is the same: each report method already
 * returns one fully-computed response object, so a PDF renderer would be
 * additive, not a rewrite.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionNames.VIEW_REPORT + "')")
@Tag(name = "Reports", description = "Project, Sprint, Worklog and Employee Productivity reports; export via format=csv|xlsx")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/projects/{projectId}")
    @Operation(summary = "Project report")
    public ResponseEntity<?> projectReport(@PathVariable UUID projectId,
                                            @RequestParam(defaultValue = "json") String format) {
        ProjectReportResponse report = reportService.projectReport(projectId);
        if (isJson(format)) {
            return ResponseEntity.ok(ApiResponse.success(report));
        }
        List<List<Object>> rows = new ArrayList<>();
        rows.add(row("Project", report.projectName()));
        rows.add(row("Status", report.status()));
        rows.add(row("Team Size", report.teamSize()));
        rows.add(row("Total Tasks", report.totalTasks()));
        rows.add(row("Total Bugs", report.totalBugs()));
        rows.add(row("Open Bugs", report.openBugs()));
        rows.add(row("Total Worklog Hours", report.totalWorklogHours()));
        for (Map.Entry<String, Long> entry : report.tasksByStatus().entrySet()) {
            rows.add(row("Tasks - " + entry.getKey(), entry.getValue()));
        }
        return export(format, "project-report-" + projectId, List.of("Metric", "Value"), rows);
    }

    @GetMapping("/sprints/{sprintId}")
    @Operation(summary = "Sprint report")
    public ResponseEntity<?> sprintReport(@PathVariable UUID sprintId,
                                           @RequestParam(defaultValue = "json") String format) {
        SprintReportResponse report = reportService.sprintReport(sprintId);
        if (isJson(format)) {
            return ResponseEntity.ok(ApiResponse.success(report));
        }
        List<List<Object>> rows = new ArrayList<>();
        rows.add(row("Sprint", report.sprintName()));
        rows.add(row("Status", report.status()));
        rows.add(row("Total Tasks", report.totalTasks()));
        rows.add(row("Completed Tasks", report.completedTasks()));
        rows.add(row("Total Estimated Hours", report.totalEstimatedHours()));
        rows.add(row("Total Actual Hours", report.totalActualHours()));
        for (Map.Entry<String, Long> entry : report.tasksByStatus().entrySet()) {
            rows.add(row("Tasks - " + entry.getKey(), entry.getValue()));
        }
        return export(format, "sprint-report-" + sprintId, List.of("Metric", "Value"), rows);
    }

    @GetMapping("/worklogs")
    @Operation(summary = "Worklog report for a project over a date range")
    public ResponseEntity<?> worklogReport(@RequestParam UUID projectId,
                                            @RequestParam LocalDate from,
                                            @RequestParam LocalDate to,
                                            @RequestParam(defaultValue = "json") String format) {
        WorklogReportResponse report = reportService.worklogReport(projectId, from, to);
        if (isJson(format)) {
            return ResponseEntity.ok(ApiResponse.success(report));
        }
        List<List<Object>> rows = new ArrayList<>();
        for (WorklogReportResponse.Entry e : report.entries()) {
            rows.add(row(e.logDate(), e.employeeName() != null ? e.employeeName() : "",
                    e.taskTitle() != null ? e.taskTitle() : "", e.hours(), e.notes() != null ? e.notes() : ""));
        }
        return export(format, "worklog-report-" + projectId, List.of("Date", "Employee", "Task", "Hours", "Notes"), rows);
    }

    @GetMapping("/employees/{employeeId}/productivity")
    @Operation(summary = "Employee productivity report over a date range")
    public ResponseEntity<?> employeeProductivityReport(@PathVariable UUID employeeId,
                                                          @RequestParam LocalDate from,
                                                          @RequestParam LocalDate to,
                                                          @RequestParam(defaultValue = "json") String format) {
        EmployeeProductivityReportResponse report = reportService.employeeProductivityReport(employeeId, from, to);
        if (isJson(format)) {
            return ResponseEntity.ok(ApiResponse.success(report));
        }
        List<List<Object>> rows = new ArrayList<>();
        rows.add(row("Employee", report.employeeName() != null ? report.employeeName() : employeeId.toString()));
        rows.add(row("Tasks Completed", report.tasksCompleted()));
        rows.add(row("Total Hours Logged", report.totalHoursLogged()));
        rows.add(row("Average Hours / Day", report.averageHoursPerDay()));
        return export(format, "employee-productivity-" + employeeId, List.of("Metric", "Value"), rows);
    }

    // ------------------------------------------------------------------

    private boolean isJson(String format) {
        return !"csv".equalsIgnoreCase(format) && !"xlsx".equalsIgnoreCase(format);
    }

    /** Builds a heterogeneous row without relying on List.of()'s generic inference to settle on Object. */
    private List<Object> row(Object... values) {
        return Arrays.asList(values);
    }

    private ResponseEntity<byte[]> export(String format, String filenameBase, List<String> headers, List<List<Object>> rows) {
        if ("xlsx".equalsIgnoreCase(format)) {
            byte[] body = ExcelWriter.write("Report", headers, rows);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(filenameBase + ".xlsx").build().toString())
                    .body(body);
        }
        byte[] body = CsvWriter.write(headers, rows).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filenameBase + ".csv").build().toString())
                .body(body);
    }
}
