package com.example.CWMS.service;

import com.example.CWMS.iservice.AuditService;
import com.example.CWMS.iservice.AuthService;
import com.example.CWMS.iservice.LoginAttemptService;
import com.example.CWMS.model.MenuItem;
import com.example.CWMS.model.User;
import com.example.CWMS.payload.LoginRequest;
import com.example.CWMS.payload.LoginResponse;
import com.example.CWMS.payload.SessionContext;
import com.example.CWMS.repository.MenuItemRepository;
import com.example.CWMS.repository.UserRepository;
import com.example.CWMS.Security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils              jwtUtils;
    private final UserRepository        userRepository;
    private final MenuItemRepository    menuItemRepository;
    private final AuditService          auditService;
    private final LoginAttemptService   loginAttemptService;

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {

        final String username  = request.getUsername();
        final String ip        = extractIp(httpRequest);
        final String userAgent = httpRequest.getHeader("User-Agent");
        final String sessionId = UUID.randomUUID().toString();

        // SQL #1 — isBlocked() : inévitable avant l'authentification
        if (loginAttemptService.isBlocked(username)) {
            auditService.logLogin(username, ip, userAgent, false, sessionId);
            throw new LockedException(
                    "Compte bloqué suite à 3 tentatives. Contactez l'administrateur.");
        }

        // SQL #2 — UserDetailsServiceImpl : inévitable pour vérifier le mot de passe
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            // Recharge le user pour incrémenter failedAttempts
            userRepository.findByUsernameWithRoleAndSite(username)
                    .ifPresent(loginAttemptService::loginFailed);
            int remaining = loginAttemptService.getRemainingAttempts(username);
            auditService.logLogin(username, ip, userAgent, false, sessionId);
            throw new BadCredentialsException(remaining > 0
                    ? "Identifiants incorrects. Tentatives restantes : " + remaining
                    : "Compte bloqué après trop de tentatives.");
        } catch (DisabledException e) {
            auditService.logLogin(username, ip, userAgent, false, sessionId);
            throw e;
        }

        final String token = jwtUtils.generateJwtToken(authentication);

        // SQL #3 — chargement entité User : 1 seule fois, passée à tout le reste
        final User user = userRepository
                .findByUsernameWithRoleAndSite(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // loginSucceeded reçoit l'entité — ZÉRO SQL supplémentaire
        loginAttemptService.loginSucceeded(user);

        // SQL #4 — menus du rôle
        final List<MenuItem> menus = menuItemRepository
                .findMenuItemsByRoleId(user.getRole().getRoleId());

        // SQL #5 — INSERT audit async (thread séparé)
        auditService.logLoginWithUser(user, ip, userAgent, true, sessionId);

        return LoginResponse.builder()
                .token(token)
                .sessionContext(buildSessionContext(user, menus))
                .build();
    }

    @Override
    public void logout(HttpServletRequest httpRequest) {
        auditService.logLogout(
                extractUsernameFromToken(httpRequest),
                extractIp(httpRequest),
                httpRequest.getHeader("X-Session-Id")
        );
    }

    private SessionContext buildSessionContext(User user, List<MenuItem> menus) {
        List<SessionContext.MenuItemInfo> menuInfos = menus.stream()
                .map(m -> SessionContext.MenuItemInfo.builder()
                        .menuItemId(m.getMenuItemId())
                        .label(m.getLabel())
                        .icon(m.getIcon())
                        .link(m.getLink())
                        .parentId(m.getParent() != null
                                ? m.getParent().getMenuItemId() : null)
                        .isTitle(m.getIsTitle())
                        .isLayout(m.getIsLayout())
                        .build())
                .collect(Collectors.toList());

        return SessionContext.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mustChangePassword(user.isMustChangePassword())
                .accountNonLocked(
                        user.getAccountNonLocked() != null
                                && user.getAccountNonLocked())
                .roleId(user.getRole().getRoleId())
                .roleName(user.getRole().getRoleName())
                .roleDescription(user.getRole().getDescription())
                .siteId(user.getSite() != null ? user.getSite().getSiteId() : null)
                .siteName(user.getSite() != null ? user.getSite().getSiteName() : null)
                .menus(menuInfos)
                .build();
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }

    private String extractUsernameFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try { return jwtUtils.getUserNameFromJwtToken(header.substring(7)); }
            catch (Exception ignored) {}
        }
        return "unknown";
    }
}