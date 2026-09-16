package com.teamflow.ai.project.service;

import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.project.client.EmployeeClient;
import com.teamflow.ai.project.client.EmployeeSummary;
import com.teamflow.ai.project.dto.response.GlobalSearchResponse;
import com.teamflow.ai.project.dto.response.SearchResultItem;
import com.teamflow.ai.project.entity.Client;
import com.teamflow.ai.project.entity.Meeting;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.entity.Task;
import com.teamflow.ai.project.repository.ClientRepository;
import com.teamflow.ai.project.repository.MeetingRepository;
import com.teamflow.ai.project.repository.ProjectRepository;
import com.teamflow.ai.project.repository.TaskRepository;
import com.teamflow.ai.project.repository.spec.ProjectSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Global Search across the delivery domain: Projects, Tasks, Clients, Meetings,
 * and (via a synchronous Feign call, same pattern as {@code EmployeeLookupService})
 * Employees.
 *
 * <p>Deliberately excludes Notifications: those are personal to the requesting
 * employee (see {@code NotificationController}), so a company-wide keyword search
 * over them has no real business meaning — each employee already searches their
 * own via {@code GET /api/v1/notifications}. Left out rather than added for
 * completeness' sake, per "avoid unnecessary endpoints."
 *
 * <p>Each category is capped at 5 results — this endpoint is a fast, broad
 * command-palette-style lookup, not a substitute for each entity's own paginated
 * search (Project/Task/Client all already support full filtering and pagination
 * on their own endpoints).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private static final int LIMIT = 5;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;
    private final MeetingRepository meetingRepository;
    private final EmployeeClient employeeClient;

    public GlobalSearchResponse search(String keyword) {
        String trimmed = keyword == null ? "" : keyword.trim();

        return GlobalSearchResponse.builder()
                .projects(searchProjects(trimmed))
                .tasks(searchTasks(trimmed))
                .clients(searchClients(trimmed))
                .meetings(searchMeetings(trimmed))
                .employees(searchEmployees(trimmed))
                .build();
    }

    private List<SearchResultItem> searchProjects(String keyword) {
        Specification<Project> spec = Specification.allOf(
                Stream.of(
                        ProjectSpecifications.notDeleted(),
                        ProjectSpecifications.nameOrCodeContains(keyword)
                ).filter(Objects::nonNull).collect(Collectors.toList())
        );
        return projectRepository.findAll(spec, PageRequest.of(0, LIMIT)).getContent().stream()
                .map(p -> SearchResultItem.builder()
                        .type("PROJECT").id(p.getId()).title(p.getName())
                        .subtitle("%s · %s".formatted(p.getCode(), p.getStatus()))
                        .build())
                .toList();
    }

    private List<SearchResultItem> searchTasks(String keyword) {
        List<Task> tasks = keyword.isBlank() ? List.of()
                : taskRepository.findTop5ByTitleContainingIgnoreCaseAndDeletedFalse(keyword);
        return tasks.stream()
                .map(t -> SearchResultItem.builder()
                        .type("TASK").id(t.getId()).title(t.getTitle())
                        .subtitle("%s · %s".formatted(t.getStatus(), t.getPriority()))
                        .build())
                .toList();
    }

    private List<SearchResultItem> searchClients(String keyword) {
        List<Client> clients = keyword.isBlank() ? List.of()
                : clientRepository.findTop5ByNameContainingIgnoreCaseAndDeletedFalse(keyword);
        return clients.stream()
                .map(c -> SearchResultItem.builder()
                        .type("CLIENT").id(c.getId()).title(c.getName()).subtitle(c.getCode())
                        .build())
                .toList();
    }

    private List<SearchResultItem> searchMeetings(String keyword) {
        List<Meeting> meetings = keyword.isBlank() ? List.of()
                : meetingRepository.findTop5ByTitleContainingIgnoreCaseAndDeletedFalse(keyword);
        return meetings.stream()
                .map(m -> SearchResultItem.builder()
                        .type("MEETING").id(m.getId()).title(m.getTitle())
                        .subtitle(DATE_FORMAT.format(m.getScheduledAt().atZone(java.time.ZoneId.systemDefault())))
                        .build())
                .toList();
    }

    /** identity-service unreachable must degrade this one category to empty, never fail the whole search. */
    private List<SearchResultItem> searchEmployees(String keyword) {
        if (keyword.isBlank()) {
            return List.of();
        }
        try {
            ApiResponse<PageResponse<EmployeeSummary>> response = employeeClient.search(keyword, LIMIT);
            PageResponse<EmployeeSummary> page = response.getData();
            if (page == null) {
                return List.of();
            }
            return page.content().stream()
                    .map(e -> SearchResultItem.builder()
                            .type("EMPLOYEE").id(e.id()).title(e.fullName())
                            .subtitle(e.active() ? "Active" : "Inactive")
                            .build())
                    .toList();
        } catch (Exception ex) {
            log.warn("identity-service unreachable during global search: {}", ex.getMessage());
            return List.of();
        }
    }
}
