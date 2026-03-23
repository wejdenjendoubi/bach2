package com.example.CWMS.iservice;

import com.example.CWMS.dto.AuditLogDTO;
import com.example.CWMS.model.AuditLog.EventType;
import com.example.CWMS.model.AuditLog.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service dédié aux requêtes de lecture audit (séparation Command/Query).
 * AuditService gère l'écriture. AuditQueryService gère la lecture.
 */
public interface AuditQueryService {

    Page<AuditLogDTO> search(EventType eventType, Severity severity,
                             Integer userId, LocalDateTime from,
                             LocalDateTime to, Pageable pageable);

    Page<AuditLogDTO> getByUser(Integer userId, Pageable pageable);

    List<AuditLogDTO> getConnections(Integer userId);
}