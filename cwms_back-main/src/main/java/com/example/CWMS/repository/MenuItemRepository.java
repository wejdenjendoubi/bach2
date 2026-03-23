package com.example.CWMS.repository;

import com.example.CWMS.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

    /**
     * 1 seule requête SQL via JOIN direct sur RoleMenuMappings.
     * Appelée une fois dans AuthServiceImpl après chargement du User.
     *
     * Pas de JOIN FETCH sur parent ici : MenuItem.parent est un
     * auto-référencement, Hibernate le résout via le ParentId
     * déjà présent dans le résultat — pas de requête lazy supplémentaire
     * si on accède seulement à parent.menuItemId dans le DTO.
     */
    @Query("""
        SELECT m FROM MenuItem m
        JOIN RoleMenuMapping rmm ON rmm.menuItem.menuItemId = m.menuItemId
        WHERE rmm.role.roleId = :roleId
        ORDER BY m.menuItemId
    """)
    List<MenuItem> findMenuItemsByRoleId(@Param("roleId") Integer roleId);
}