package com.example.CWMS.service;

import com.example.CWMS.dto.AuditLogDTO;
import com.example.CWMS.iservice.AuditQueryService;
import com.example.CWMS.model.AuditLog.EventType;
import com.example.CWMS.model.AuditLog.Severity;
import com.example.CWMS.model.User;
import com.example.CWMS.repository.AuditLogRepository;
import com.example.CWMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditQueryServiceImpl implements AuditQueryService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository     userRepository;

    @Override
    public Page<AuditLogDTO> search(EventType eventType, Severity severity,
                                    Integer userId, LocalDateTime from,
                                    LocalDateTime to, Pageable pageable) {
        /*
         * searchWithUser : JOIN FETCH user+role+site dans la même requête.
         * AuditLogDTO.from() accède à log.getUser().getRole() et .getSite()
         * sans déclencher de lazy load.
         *
         * countQuery séparée obligatoire avec Pageable + JOIN FETCH
         * sur SQL Server (sinon erreur HibernateQueryException).
         */
        return auditLogRepository
                .searchWithUser(eventType, severity, userId, from, to, pageable)
                .map(AuditLogDTO::from);
    }

    @Override
    public Page<AuditLogDTO> getByUser(Integer userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur non trouvé : " + userId));
        return auditLogRepository.findByUser(user, pageable)
                .map(AuditLogDTO::from);
    }

    @Override
    public List<AuditLogDTO> getConnections(Integer userId) {
        return auditLogRepository.findConnectionsByUserId(userId)
                .stream()
                .map(AuditLogDTO::from)
                .toList();
    }
}