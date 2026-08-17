package br.com.nextagon.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticatedUser {

    private AuthenticatedUser() {}

    /**
     * Retorna o userId do token JWT do usuário autenticado na requisição atual.
     */
    public static String getId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("Usuário não autenticado");
        }
        return auth.getName(); // getName() retorna o subject do JWT = userId
    }
}