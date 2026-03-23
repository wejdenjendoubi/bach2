package com.example.CWMS.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de session retourné au frontend après login réussi.
 *
 * Contient UNIQUEMENT ce qui est nécessaire au frontend :
 *   - identité de l'utilisateur
 *   - rôle (pour les guards Angular)
 *   - site (pour le contexte métier)
 *   - menus autorisés (pour la sidebar dynamique)
 *
 * Aucune donnée sensible (passwordHash, failedAttempts, etc.).
 * Aucun objet JPA exposé directement.
 * Immuable côté frontend — rechargé uniquement si le rôle change.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionContext {

    // ── Utilisateur ───────────────────────────────────────────────────────────
    private Integer userId;
    private String  username;
    private String  email;
    private String  firstName;
    private String  lastName;
    private boolean mustChangePassword;
    private boolean accountNonLocked;

    // ── Rôle ──────────────────────────────────────────────────────────────────
    private Integer roleId;
    private String  roleName;
    private String  roleDescription;

    // ── Site ──────────────────────────────────────────────────────────────────
    private Integer siteId;
    private String  siteName;

    // ── Menus autorisés pour ce rôle ──────────────────────────────────────────
    private List<MenuItemInfo> menus;

    /**
     * Représentation légère d'un item de menu.
     * Pas d'entité JPA — données minimales pour construire la sidebar.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemInfo {
        private Integer menuItemId;
        private String  label;
        private String  icon;
        private String  link;
        private Integer parentId;
        private Boolean isTitle;
        private Boolean isLayout;
    }
}