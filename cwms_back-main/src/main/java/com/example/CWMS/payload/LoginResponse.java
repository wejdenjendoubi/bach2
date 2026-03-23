package com.example.CWMS.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse unique du endpoint /login.
 * Le frontend n'a besoin d'aucun autre appel après réception de cet objet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String         token;
    private SessionContext sessionContext;
}