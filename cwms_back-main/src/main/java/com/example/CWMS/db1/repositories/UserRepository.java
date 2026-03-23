package com.example.CWMS.db1.repositories;

import com.example.CWMS.db1.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Requête principale utilisée partout dans le flux login.
     * 1 seule requête SQL avec LEFT JOIN FETCH sur Role et Site.
     * Élimine les lazy loads séparés select Roles + select Sites.
     */
    @Query("""
        SELECT u FROM User u
        LEFT JOIN FETCH u.role
        LEFT JOIN FETCH u.site
        WHERE u.username = :username
    """)
    Optional<User> findByUsernameWithRoleAndSite(@Param("username") String username);

    /**
     * Charge tous les users avec Role et Site en 1 requête.
     * Remplace findAll() dans UserServiceImpl.getAllUsers().
     */
    @Query("""
        SELECT u FROM User u
        LEFT JOIN FETCH u.role
        LEFT JOIN FETCH u.site
    """)
    List<User> findAllWithRoleAndSite();

    // ── Méthodes standards conservées ────────────────────────────────────────

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByIsActive(Boolean isActive);

    @Query("SELECT u FROM User u WHERE u.role.roleId = :roleId")
    List<User> findByRoleId(Integer roleId);

    @Query("UPDATE User u SET u.failedAttempts = ?2 WHERE u.username = ?1")
    @Modifying
    @Transactional
    void updateFailedAttempts(String username, int failAttempts);

    @Modifying
    @Query(value = "UPDATE audit_logs SET user_id = NULL WHERE user_id = :userId",
            nativeQuery = true)
    void detachAuditLogs(@Param("userId") Integer userId);
}