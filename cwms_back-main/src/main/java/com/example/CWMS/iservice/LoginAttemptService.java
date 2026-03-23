package com.example.CWMS.iservice;

import com.example.CWMS.model.User;

public interface LoginAttemptService {

    /**
     * Variante optimisée : reçoit l'entité User déjà chargée.
     * Zéro requête SQL — utilisée par AuthServiceImpl.
     */
    void loginSucceeded(User user);

    /**
     * Variante optimisée : reçoit l'entité User déjà chargée.
     * Zéro requête SQL — utilisée par AuthServiceImpl.
     */
    void loginFailed(User user);

    /**
     * Variante par username : utilisée pour isBlocked() AVANT
     * que l'entité User soit chargée.
     */
    boolean isBlocked(String username);

    int getRemainingAttempts(String username);
}