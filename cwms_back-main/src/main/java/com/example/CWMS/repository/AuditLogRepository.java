package com.example.CWMS.repository;

import com.example.CWMS.model.AuditLog;
import com.example.CWMS.model.AuditLog.EventType;
import com.example.CWMS.model.AuditLog.Severity;
import com.example.CWMS.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /*
     * ✅ Requête principale avec JOIN FETCH sur user+role+site.
     * Évite les 2 requêtes lazy (select Users where UserId + select Roles + select Sites)
     * déclenchées par AuditLogDTO.from() quand log.getUser() est accédé.
     *
     * countQuery OBLIGATOIRE avec JOIN FETCH + Pageable sur SQL Server :
     * Spring Data ne peut pas dériver le COUNT automatiquement depuis
     * un FETCH JOIN — on le fournit explicitement pour éviter l'erreur
     * "query specified join fetching but owner of fetched association
     * was not present in the SELECT list".
     */
    @Query(
            value = """
            SELECT a FROM AuditLog a
            LEFT JOIN FETCH a.user u
            LEFT JOIN FETCH u.role
            LEFT JOIN FETCH u.site
            WHERE (:eventType IS NULL OR a.eventType  = :eventType)
              AND (:severity  IS NULL OR a.severity   = :severity)
              AND (:userId    IS NULL OR a.user.userId = :userId)
              AND (:from      IS NULL OR a.createdAt  >= :from)
              AND (:to        IS NULL OR a.createdAt  <= :to)
        """,
            countQuery = """
            SELECT COUNT(a) FROM AuditLog a
            WHERE (:eventType IS NULL OR a.eventType  = :eventType)
              AND (:severity  IS NULL OR a.severity   = :severity)
              AND (:userId    IS NULL OR a.user.userId = :userId)
              AND (:from      IS NULL OR a.createdAt  >= :from)
              AND (:to        IS NULL OR a.createdAt  <= :to)
        """
    )
    Page<AuditLog> searchWithUser(
            @Param("eventType") EventType     eventType,
            @Param("severity")  Severity      severity,
            @Param("userId")    Integer       userId,
            @Param("from")      LocalDateTime from,
            @Param("to")        LocalDateTime to,
            Pageable pageable
    );

    /*
     * Conservé pour rétrocompatibilité — utilisé par AuditQueryServiceImpl.search()
     * si searchWithUser n'est pas encore en place.
     * À terme : ne garder que searchWithUser.
     */
    @Query(
            value = """
            SELECT a FROM AuditLog a
            WHERE (:eventType IS NULL OR a.eventType  = :eventType)
              AND (:severity  IS NULL OR a.severity   = :severity)
              AND (:userId    IS NULL OR a.user.userId = :userId)
              AND (:from      IS NULL OR a.createdAt  >= :from)
              AND (:to        IS NULL OR a.createdAt  <= :to)
        """,
            countQuery = """
            SELECT COUNT(a) FROM AuditLog a
            WHERE (:eventType IS NULL OR a.eventType  = :eventType)
              AND (:severity  IS NULL OR a.severity   = :severity)
              AND (:userId    IS NULL OR a.user.userId = :userId)
              AND (:from      IS NULL OR a.createdAt  >= :from)
              AND (:to        IS NULL OR a.createdAt  <= :to)
        """
    )
    Page<AuditLog> search(
            @Param("eventType") EventType     eventType,
            @Param("severity")  Severity      severity,
            @Param("userId")    Integer       userId,
            @Param("from")      LocalDateTime from,
            @Param("to")        LocalDateTime to,
            Pageable pageable
    );

    Page<AuditLog> findByUser(User user, Pageable pageable);

    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.user.userId = :userId
          AND a.eventType IN ('LOGIN', 'LOGOUT', 'LOGIN_FAILED')
        ORDER BY a.createdAt DESC
    """)
    List<AuditLog> findConnectionsByUserId(@Param("userId") Integer userId);

    @Query("""
        SELECT COUNT(a) FROM AuditLog a
        WHERE a.username  = :username
          AND a.eventType = com.example.CWMS.model.AuditLog.EventType.LOGIN_FAILED
          AND a.createdAt >= :since
    """)
    long countRecentFailedLogins(@Param("username") String username,
                                 @Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Integer userId);
}