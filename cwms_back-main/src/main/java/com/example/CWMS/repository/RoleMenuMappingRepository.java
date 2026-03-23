package com.example.CWMS.repository;

import com.example.CWMS.model.RoleMenuMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface RoleMenuMappingRepository extends JpaRepository<RoleMenuMapping, Integer> {

    @Query("SELECT rmm FROM RoleMenuMapping rmm WHERE rmm.role.roleId = :roleId")
    List<RoleMenuMapping> findByRoleId(@Param("roleId") Integer roleId);

    @Query("SELECT rmm.menuItem.menuItemId FROM RoleMenuMapping rmm " +
            "WHERE rmm.role.roleId = :roleId")
    List<Integer> findMenuItemIdsByRoleId(@Param("roleId") Integer roleId);

    /*
     * ✅ Requête groupée : récupère les menuItemIds pour TOUS les rôles
     * en une seule requête SQL au lieu de N requêtes dans toDTO().
     *
     * Retourne une Map<roleId, List<menuItemId>> construite en Java
     * depuis le résultat brut.
     *
     * Usage : appelée une seule fois dans getAllRoles() puis
     * distribuée à chaque DTO sans aller en base.
     */
    @Query("SELECT rmm.role.roleId, rmm.menuItem.menuItemId " +
            "FROM RoleMenuMapping rmm")
    List<Object[]> findAllRoleMenuMappings();

    /*
     * ✅ Requête groupée : compte les users par rôle en une seule requête.
     * Évite N appels à findByRoleId(roleId).size() dans toDTO().
     */
    @Query("SELECT u.role.roleId, COUNT(u) FROM User u " +
            "WHERE u.role IS NOT NULL GROUP BY u.role.roleId")
    List<Object[]> countUsersByRole();

    @Modifying
    @Query("DELETE FROM RoleMenuMapping rmm WHERE rmm.role.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Integer roleId);

    boolean existsByRole_RoleIdAndMenuItem_MenuItemId(Integer roleId, Integer menuItemId);
}