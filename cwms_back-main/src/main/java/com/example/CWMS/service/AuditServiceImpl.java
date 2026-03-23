package com.example.CWMS.service;

import com.example.CWMS.iservice.AuditService;
import com.example.CWMS.model.AuditLog;
import com.example.CWMS.model.AuditLog.EventType;
import com.example.CWMS.model.AuditLog.Severity;
import com.example.CWMS.model.User;
import com.example.CWMS.repository.AuditLogRepository;
import com.example.CWMS.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository     userRepository;
    private final ObjectMapper       objectMapper;

    // ── CONNEXIONS ────────────────────────────────────────────────────────────

    /**
     * Variante standard — utilisée pour les échecs (user peut ne pas exister).
     */
    @Async("auditExecutor")
    @Override
    public void logLogin(String username, String ip, String userAgent,
                         boolean success, String sessionId) {
        User user = userRepository.findByUsernameWithRoleAndSite(username).orElse(null);
        persistLogin(user, username, ip, userAgent, success, sessionId);
    }

    /**
     * Variante optimisée — appelée par AuthServiceImpl après succès.
     * L'entité User est déjà chargée : ZÉRO requête SQL supplémentaire.
     * Le thread async reçoit la référence détachée — Hibernate ne
     * recharge pas une entité détachée si on ne touche qu'à ses champs
     * déjà initialisés (username, userId).
     */
    @Async("auditExecutor")
    @Override
    public void logLoginWithUser(User user, String ip, String userAgent,
                                 boolean success, String sessionId) {
        persistLogin(
                user,
                user != null ? user.getUsername() : "unknown",
                ip, userAgent, success, sessionId
        );
    }

    @Async("auditExecutor")
    @Override
    public void logLogout(String username, String ip, String sessionId) {
        User user = userRepository.findByUsernameWithRoleAndSite(username).orElse(null);
        save(AuditLog.builder()
                .eventType(EventType.LOGOUT)
                .severity (Severity.INFO)
                .action   ("DECONNEXION")
                .user     (user)
                .username (username)
                .ipAddress(ip)
                .sessionId(sessionId)
                .build());
    }

    // ── ACTIONS CRUD ──────────────────────────────────────────────────────────

    @Async("auditExecutor")
    @Override
    public void logAction(String action, String entityType, String entityId,
                          Object oldObj, Object newObj) {
        try {
            AuditLog.AuditLogBuilder builder = AuditLog.builder()
                    .eventType    (resolveEventType(action))
                    .severity     (Severity.INFO)
                    .action       (action)
                    .entityType   (entityType)
                    .entityId     (entityId)
                    .oldValue     (toJson(oldObj))
                    .newValue     (toJson(newObj))
                    .correlationId(UUID.randomUUID().toString());
            enrichWithCurrentUser(builder);
            save(builder.build());
        } catch (Exception e) {
            log.error("Erreur log action: {}", e.getMessage());
        }
    }

    @Async("auditExecutor")
    @Override
    public void logActionWithUsername(String action, String entityType,
                                      String entityId, String snapshotUsername,
                                      Object oldObj, Object newObj) {
        try {
            AuditLog.AuditLogBuilder builder = AuditLog.builder()
                    .eventType    (resolveEventType(action))
                    .severity     (Severity.INFO)
                    .action       (action)
                    .entityType   (entityType)
                    .entityId     (entityId)
                    .username     (snapshotUsername)
                    .oldValue     (toJson(oldObj))
                    .newValue     (toJson(newObj))
                    .correlationId(UUID.randomUUID().toString());
            enrichWithCurrentUser(builder);
            save(builder.build());
        } catch (Exception e) {
            log.error("Erreur logActionWithUsername: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedCreation(String targetValue, String reason, String details) {
        try {
            AuditLog.AuditLogBuilder builder = AuditLog.builder()
                    .eventType    (EventType.CREATE_FAILED)
                    .severity     (Severity.WARNING)
                    .action       ("USER_CREATE_FAILED")
                    .entityType   ("User")
                    .entityId     (targetValue)
                    .errorMessage (details)
                    .oldValue     (reason)
                    .correlationId(UUID.randomUUID().toString());
            enrichWithCurrentUser(builder);
            save(builder.build());
        } catch (Exception e) {
            log.error("Erreur logFailedCreation: {}", e.getMessage());
        }
    }

    @Async("auditExecutor")
    @Override
    public void logError(Exception ex, HttpServletRequest request, int statusCode) {
        AuditLog.AuditLogBuilder builder = AuditLog.builder()
                .eventType   (EventType.ERROR)
                .severity    (statusCode >= 500 ? Severity.CRITICAL : Severity.ERROR)
                .ipAddress   (extractClientIp(request))
                .httpMethod  (request.getMethod())
                .endpoint    (request.getRequestURI())
                .statusCode  (statusCode)
                .errorMessage(ex.getMessage())
                .stackTrace  (truncateStackTrace(ex));
        enrichWithCurrentUser(builder);
        save(builder.build());
    }

    @Async("auditExecutor")
    @Override
    public void logHttpError(HttpServletRequest request, int statusCode, long durationMs) {
        AuditLog.AuditLogBuilder builder = AuditLog.builder()
                .eventType (EventType.ERROR)
                .severity  (statusCode >= 500 ? Severity.CRITICAL : Severity.ERROR)
                .ipAddress (extractClientIp(request))
                .httpMethod(request.getMethod())
                .endpoint  (request.getRequestURI())
                .statusCode(statusCode)
                .durationMs(durationMs);
        enrichWithCurrentUser(builder);
        save(builder.build());
    }

    // ── UTILITAIRES ───────────────────────────────────────────────────────────

    @Override
    public void enrichWithCurrentUser(AuditLog.AuditLogBuilder builder) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || "anonymousUser".equals(auth.getPrincipal())) return;

            /*
             * Avec le nouveau JwtFilter, auth.getPrincipal() est une String
             * (le username extrait du token), pas un UserDetails.
             * On supporte les deux cas pour la rétrocompatibilité.
             */
            String username;
            Object principal = auth.getPrincipal();
            if (principal instanceof String s) {
                username = s;
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
                username = ud.getUsername();
            } else {
                username = auth.getName();
            }

            builder.username(username);
            userRepository.findByUsernameWithRoleAndSite(username)
                    .filter(u -> u.getUserId() != null)
                    .ifPresent(builder::user);

        } catch (Exception e) {
            log.warn("enrichWithCurrentUser ignoré: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(AuditLog auditLog) {
        try {
            AuditLog saved = auditLogRepository.save(auditLog);
            log.info("Audit — id={} type={} user={}",
                    saved.getId(), saved.getEventType(), saved.getUsername());
        } catch (Exception e) {
            log.error("Erreur sauvegarde audit: {}", e.getMessage(), e);
        }
    }

    @Override
    public String toJson(Object obj) {
        if (obj == null) return null;
        try { return objectMapper.writeValueAsString(obj); }
        catch (JsonProcessingException e) { return obj.toString(); }
    }

    @Override
    public EventType resolveEventType(String action) {
        if (action == null) return EventType.READ;
        String a = action.toUpperCase();
        if (a.contains("CREATE") || a.contains("ADD")    || a.contains("SAVE"))    return EventType.CREATE;
        if (a.contains("UPDATE") || a.contains("EDIT")   || a.contains("MODIF"))   return EventType.UPDATE;
        if (a.contains("DELETE") || a.contains("REMOVE") || a.contains("SUPPRIM")) return EventType.DELETE;
        if (a.contains("FAILED") || a.contains("ECHEC"))                           return EventType.CREATE_FAILED;
        return EventType.READ;
    }

    @Override
    public String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }

    @Override
    public String truncateStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String t = sw.toString();
        return t.length() > 4000 ? t.substring(0, 4000) + "..." : t;
    }

    // ── Privé ─────────────────────────────────────────────────────────────────

    private void persistLogin(User user, String username, String ip,
                              String userAgent, boolean success, String sessionId) {
        save(AuditLog.builder()
                .eventType (success ? EventType.LOGIN : EventType.LOGIN_FAILED)
                .severity  (success ? Severity.INFO   : Severity.WARNING)
                .action    (success ? "CONNEXION_OK"  : "TENTATIVE_ECHOUEE")
                .user      (user)
                .username  (username)
                .ipAddress (ip)
                .userAgent (userAgent)
                .sessionId (sessionId)
                .statusCode(success ? 200 : 401)
                .build());
    }
}