package com.example.CWMS.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtils jwtUtils;
    /*
     * UserDetailsService supprimé des dépendances de JwtFilter.
     * Il n'est plus utilisé ici — les authorities viennent du token.
     */

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            final String jwt = header.substring(7);

            if (jwtUtils.validateJwtToken(jwt)) {
                /*
                 * ✅ ZÉRO REQUÊTE SQL ICI.
                 *
                 * Avant : loadUserByUsername(username) → SELECT Users JOIN Roles JOIN Sites
                 * Après : extraction directe depuis les claims JWT signés
                 *
                 * Sécurité : le token est signé avec HMAC-SHA512 (clé 64+ chars).
                 * Les authorities dans le token sont intègres — impossibles à falsifier
                 * sans connaître la clé secrète.
                 *
                 * La vérification de compte désactivé/bloqué est faite au login.
                 * Entre deux logins, si un admin désactive un compte, le token
                 * reste valide jusqu'à expiration (24h) — comportement standard
                 * JWT stateless. Pour invalider immédiatement : implémenter
                 * une blacklist Redis (hors scope de cette correction).
                 */
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                List<SimpleGrantedAuthority> authorities =
                        jwtUtils.getAuthoritiesFromJwtToken(jwt);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("JWT authentifié : {} | authorities : {}",
                        username, authorities);
            } else {
                log.warn("JWT invalide : {} {}", request.getMethod(),
                        request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }
}