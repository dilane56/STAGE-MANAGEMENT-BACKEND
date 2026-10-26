package org.kfokam48.stagemanagementbackend.service.auth;

import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.model.auth.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Accès à l'utilisateur authentifié de la requête courante et contrôles d'appartenance
 * (empêche un utilisateur d'agir sur les ressources d'un autre en changeant un ID dans l'URL).
 */
@Service
public class CurrentUserService {

    public CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        return user;
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean hasRole(Roles role) {
        return getCurrentUser().getRole() == role;
    }

    public boolean isAdmin() {
        return hasRole(Roles.ADMIN);
    }

    /** Autorise l'administrateur ou l'utilisateur dont l'ID est passé en paramètre. */
    public void checkSelfOrAdmin(Long userId) {
        if (!isAdmin() && !getCurrentUserId().equals(userId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à accéder à cette ressource");
        }
    }

    /** Autorise uniquement l'utilisateur dont l'ID est passé en paramètre (pas de dérogation admin). */
    public void checkSelf(Long userId) {
        if (!getCurrentUserId().equals(userId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à agir au nom d'un autre utilisateur");
        }
    }
}
