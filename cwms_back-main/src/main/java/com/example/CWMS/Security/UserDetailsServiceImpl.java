package com.example.CWMS.Security;

import com.example.CWMS.model.User;
import com.example.CWMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Charge UserDetails pour Spring Security.
 *
 * Utilise findByUsernameWithRoleAndSite : 1 seule requête JOIN FETCH.
 *
 * NOTE : @Cacheable retiré volontairement.
 * Le cache AOP est inopérant sur UserDetailsService car Spring Security
 * wraps ce bean avec son propre proxy avant le proxy de cache Spring.
 * Le cache Caffeine ne s'applique donc jamais — il donnait une fausse
 * impression d'optimisation sans effet réel.
 *
 * La vraie optimisation est dans AuthServiceImpl : l'entité User chargée
 * ici n'est pas rechargée — elle est passée directement aux composants
 * qui en ont besoin.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsernameWithRoleAndSite(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé : " + username));

        String roleStr = user.getRole().getRoleName().toUpperCase();
        if (!roleStr.startsWith("ROLE_")) roleStr = "ROLE_" + roleStr;

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash() != null
                        ? user.getPasswordHash() : "")
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority(roleStr)))
                .disabled(Boolean.FALSE.equals(user.getIsActive()))
                .build();
    }
}