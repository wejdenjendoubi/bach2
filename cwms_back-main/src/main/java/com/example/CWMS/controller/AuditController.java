package com.example.CWMS.controller;

import com.example.CWMS.dto.ApiResponse;
import com.example.CWMS.dto.AuditLogDTO;
import com.example.CWMS.iservice.AuditQueryService;
import com.example.CWMS.model.AuditLog.EventType;
import com.example.CWMS.model.AuditLog.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller HTTP pur — aucune logique métier, aucun accès repository direct.
 * Délègue tout à AuditQueryService.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditQueryService auditQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> search(
            @RequestParam(required = false) String        eventType,
            @RequestParam(required = false) String        severity,
            @RequestParam(required = false) Integer       userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {

        EventType et  = parseEnum(EventType.class,  eventType);
        Severity  sev = parseEnum(Severity.class,   severity);

        return ResponseEntity.ok(ApiResponse.success(
                auditQueryService.search(et, sev, userId, from, to, pageable)
        ));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> getByUser(
            @PathVariable Integer userId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                auditQueryService.getByUser(userId, pageable)
        ));
    }

    @GetMapping("/user/{userId}/connections")
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getConnections(
            @PathVariable Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(
                auditQueryService.getConnections(userId)
        ));
    }

    // ── Privé ─────────────────────────────────────────────────────────────────
    private <E extends Enum<E>> E parseEnum(Class<E> type, String value) {
        if (value == null || value.isBlank()) return null;
        try { return Enum.valueOf(type, value); }
        catch (IllegalArgumentException e) { return null; }
    }
}