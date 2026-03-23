package com.example.CWMS.iservice;

import com.example.CWMS.model.AuditLog;
import com.example.CWMS.model.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuditService {

    void logLogin(String username, String ip, String userAgent,
                  boolean success, String sessionId);

    /**
     * Variante optimisée : reçoit l'entité User déjà chargée.
     * Zéro requête SQL supplémentaire dans l'audit au moment du login.
     */
    void logLoginWithUser(User user, String ip, String userAgent,
                          boolean success, String sessionId);

    void logLogout(String username, String ip, String sessionId);

    void logAction(String action, String entityType, String entityId,
                   Object oldObj, Object newObj);

    void logActionWithUsername(String action, String entityType, String entityId,
                               String snapshotUsername, Object oldObj, Object newObj);

    void logFailedCreation(String targetValue, String reason, String details);

    void logError(Exception ex, HttpServletRequest request, int statusCode);

    void logHttpError(HttpServletRequest request, int statusCode, long durationMs);

    void enrichWithCurrentUser(AuditLog.AuditLogBuilder builder);

    void save(AuditLog auditLog);

    String toJson(Object obj);

    AuditLog.EventType resolveEventType(String action);

    String extractClientIp(HttpServletRequest request);

    String truncateStackTrace(Exception ex);
}