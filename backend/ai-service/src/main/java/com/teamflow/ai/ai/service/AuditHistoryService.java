package com.teamflow.ai.ai.service;

import com.teamflow.ai.ai.document.AuditHistory;
import com.teamflow.ai.ai.mapper.AuditHistoryMapper;
import com.teamflow.ai.ai.dto.response.AuditHistoryResponse;
import com.teamflow.ai.ai.repository.AuditHistoryRepository;
import com.teamflow.ai.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditHistoryService {

    private final AuditHistoryRepository auditHistoryRepository;
    private final AuditHistoryMapper auditHistoryMapper;

    public PageResponse<AuditHistoryResponse> forEntity(String entityType, String entityId, Pageable pageable) {
        Page<AuditHistory> page =
                auditHistoryRepository.findAllByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable);
        return PageResponse.from(page, auditHistoryMapper::toResponse);
    }

    public PageResponse<AuditHistoryResponse> forProject(String projectId, Pageable pageable) {
        Page<AuditHistory> page =
                auditHistoryRepository.findAllByProjectIdOrderByTimestampDesc(projectId, pageable);
        return PageResponse.from(page, auditHistoryMapper::toResponse);
    }

    public PageResponse<AuditHistoryResponse> all(Pageable pageable) {
        Page<AuditHistory> page = auditHistoryRepository.findAllByOrderByTimestampDesc(pageable);
        return PageResponse.from(page, auditHistoryMapper::toResponse);
    }
}
