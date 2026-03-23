package com.example.CWMS.service;

import com.example.CWMS.iservice.LoginAttemptService;
import com.example.CWMS.model.User;
import com.example.CWMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    public static final int MAX_ATTEMPTS = 3;

    private final UserRepository userRepository;

    /**
     * Reçoit l'entité User déjà chargée par AuthServiceImpl.
     * ZÉRO requête SQL supplémentaire.
     */
    @Override
    @Transactional
    public void loginSucceeded(User user) {
        user.setFailedAttempts(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        userRepository.save(user);
    }

    /**
     * Reçoit l'entité User déjà chargée par AuthServiceImpl.
     * ZÉRO requête SQL supplémentaire.
     */
    @Override
    @Transactional
    public void loginFailed(User user) {
        int attempts = user.getFailedAttempts() != null
                ? user.getFailedAttempts() : 0;
        int newAttempts = attempts + 1;
        user.setFailedAttempts(newAttempts);

        if (newAttempts >= MAX_ATTEMPTS) {
            user.setAccountNonLocked(false);
            user.setLockTime(LocalDateTime.now());
            user.setIsActive(false);
        }
        userRepository.save(user);
    }

    /**
     * Appelée AVANT que l'entité User soit chargée dans AuthServiceImpl.
     * 1 requête SQL inévitable ici — c'est la seule acceptable avant auth.
     */
    @Override
    public boolean isBlocked(String username) {
        return userRepository.findByUsernameWithRoleAndSite(username)
                .map(u -> Boolean.FALSE.equals(u.getAccountNonLocked())
                        || Boolean.FALSE.equals(u.getIsActive()))
                .orElse(false);
    }

    @Override
    public int getRemainingAttempts(String username) {
        return userRepository.findByUsernameWithRoleAndSite(username)
                .map(u -> {
                    int attempts = u.getFailedAttempts() != null
                            ? u.getFailedAttempts() : 0;
                    return Math.max(0, MAX_ATTEMPTS - attempts);
                })
                .orElse(MAX_ATTEMPTS);
    }
}