package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.utilisateur.UtilisateurDTO;
import org.kfokam48.stagemanagementbackend.exception.ResourceAlreadyExistException;
import org.kfokam48.stagemanagementbackend.model.Utilisateur;
import org.kfokam48.stagemanagementbackend.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Règles communes de création / mise à jour des comptes (étudiant, entreprise, enseignant, admin).
 * <ul>
 *     <li>email unique (409 au lieu de 404/500 auparavant) ;</li>
 *     <li>mise à jour partielle : un champ absent (null) ne remplace plus la valeur existante ;</li>
 *     <li>mot de passe modifié uniquement s'il est fourni (auparavant BCrypt échouait sur null).</li>
 * </ul>
 */
@Component
public class UserAccountHelper {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountHelper(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void prepareNewAccount(Utilisateur user, UtilisateurDTO dto) {
        if (isBlank(dto.getEmail())) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }
        if (isBlank(dto.getPassword())) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire");
        }
        String email = dto.getEmail().trim();
        if (utilisateurRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistException("Un utilisateur existe déjà avec cet email");
        }
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setCreateAt(LocalDate.now());
    }

    public void applyCommonUpdates(Utilisateur user, UtilisateurDTO dto) {
        if (!isBlank(dto.getEmail())) {
            String email = dto.getEmail().trim();
            if (!email.equalsIgnoreCase(user.getEmail()) && utilisateurRepository.existsByEmail(email)) {
                throw new ResourceAlreadyExistException("Un utilisateur existe déjà avec cet email");
            }
            user.setEmail(email);
        }
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getTelephone() != null) {
            user.setTelephone(dto.getTelephone());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (!isBlank(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setUpdateAt(LocalDate.now());
    }

    /** Renvoie la nouvelle valeur si elle est fournie, sinon conserve l'ancienne. */
    public static <T> T keepIfNull(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
