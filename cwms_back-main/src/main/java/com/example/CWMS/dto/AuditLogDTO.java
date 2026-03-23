package com.example.CWMS.dto;

import com.example.CWMS.model.AuditLog;
import com.example.CWMS.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    private Long          id;
    private String        eventType;
    private String        severity;

    private Integer       userId;
    private String        username;
    private String        userFullName;
    private String        userRole;
    private String        userSite;

    private String        ipAddress;
    private String        httpMethod;
    private String        endpoint;
    private String        action;
    private String        entityType;
    private String        entityId;
    private String        oldValue;
    private String        newValue;
    private Integer       statusCode;
    private String        errorMessage;
    private Long          durationMs;
    private String        sessionId;
    private LocalDateTime createdAt;

    public static AuditLogDTO from(AuditLog log) {
        AuditLogDTOBuilder b = AuditLogDTO.builder()
                .id          (log.getId())
                .eventType   (log.getEventType() != null ? log.getEventType().name() : null)
                .severity    (log.getSeverity()  != null ? log.getSeverity().name()  : null)
                /*
                 * ✅ username snapshot en premier — valeur dénormalisée
                 * conservée même si log.getUser() est null (user supprimé).
                 * Elle sera écrasée par le username de l'entité User
                 * uniquement si le User est disponible.
                 */
                .username    (log.getUsername())
                .ipAddress   (log.getIpAddress())
                .httpMethod  (log.getHttpMethod())
                .endpoint    (log.getEndpoint())
                .action      (log.getAction())
                .entityType  (log.getEntityType())
                .entityId    (log.getEntityId())
                .oldValue    (log.getOldValue())
                .newValue    (log.getNewValue())
                .statusCode  (log.getStatusCode())
                .errorMessage(log.getErrorMessage())
                .durationMs  (log.getDurationMs())
                .sessionId   (log.getSessionId())
                .createdAt   (log.getCreatedAt());

        /*
         * Enrichissement depuis l'entité User si disponible.
         * Grâce au JOIN FETCH dans searchWithUser(), ce bloc
         * ne déclenche AUCUNE requête SQL supplémentaire.
         * Sans JOIN FETCH : chaque accès à u.getRole() ou u.getSite()
         * déclencherait un SELECT lazy → 2N requêtes pour N logs.
         */
        User u = log.getUser();
        if (u != null) {
            b.userId     (u.getUserId())
                    .username   (u.getUsername())
                    .userFullName(trim(u.getFirstName()) + " " + trim(u.getLastName()))
                    .userRole   (u.getRole() != null ? u.getRole().getRoleName() : null)
                    .userSite   (u.getSite() != null ? u.getSite().getSiteName() : null);
        }

        return b.build();
    }

    private static String trim(String s) {
        return s != null ? s.trim() : "";
    }
}