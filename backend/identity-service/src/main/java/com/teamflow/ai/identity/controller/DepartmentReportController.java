package com.teamflow.ai.identity.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.util.CsvWriter;
import com.teamflow.ai.identity.dto.response.DepartmentReportResponse;
import com.teamflow.ai.identity.service.DepartmentReportService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Department Report: headcount and leave summary. CSV export via format=csv, matching project-service's reports. */
@RestController
@RequestMapping("/api/v1/reports/departments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionNames.VIEW_REPORT + "')")
@Tag(name = "Reports", description = "Department headcount and leave summary")
public class DepartmentReportController {

    private final DepartmentReportService departmentReportService;

    @GetMapping("/{departmentId}")
    @Operation(summary = "Department report")
    public ResponseEntity<?> report(@PathVariable UUID departmentId,
                                     @RequestParam(defaultValue = "json") String format) {
        DepartmentReportResponse report = departmentReportService.report(departmentId);
        if (!"csv".equalsIgnoreCase(format)) {
            return ResponseEntity.ok(ApiResponse.success(report));
        }

        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Department", report.departmentName()));
        rows.add(Arrays.asList("Total Employees", report.totalEmployees()));
        rows.add(Arrays.asList("Active Employees", report.activeEmployees()));
        rows.add(Arrays.asList("Inactive Employees", report.inactiveEmployees()));
        rows.add(Arrays.asList("Pending Leave Requests", report.pendingLeaveRequests()));

        byte[] body = CsvWriter.write(List.of("Metric", "Value"), rows).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("department-report-" + departmentId + ".csv").build().toString())
                .body(body);
    }
}
