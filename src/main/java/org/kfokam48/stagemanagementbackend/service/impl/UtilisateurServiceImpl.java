package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.utilisateur.Contact;
import org.kfokam48.stagemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.UtilisateurMappeur;
import org.kfokam48.stagemanagementbackend.model.Enseignant;
import org.kfokam48.stagemanagementbackend.model.Utilisateur;
import org.kfokam48.stagemanagementbackend.repository.EnseignantRepository;
import org.kfokam48.stagemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.stagemanagementbackend.service.UtilisateurService;
import org.kfokam48.stagemanagementbackend.service.auth.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMappeur utilisateurMappeur;
    private final EnseignantRepository enseignantRepository;
    private final CurrentUserService currentUserService;

    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository, UtilisateurMappeur utilisateurMappeur,
                                  EnseignantRepository enseignantRepository, CurrentUserService currentUserService) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurMappeur = utilisateurMappeur;
        this.enseignantRepository = enseignantRepository;
        this.currentUserService = currentUserService;
    }


    @Override
    public UtilisateurResponseDTO getUtilisateurById(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur not found"));
        return  utilisateurMappeur.utilisateurToUtilisateurResponseDTO(utilisateur);
    }



    @Override
    public UtilisateurResponseDTO getUtilisateurByEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur not found"));
        return utilisateurMappeur.utilisateurToUtilisateurResponseDTO(utilisateur);
    }

    @Override
    public List<UtilisateurResponseDTO> getAllUtilisateurs() {
        return utilisateurMappeur.utilisateursToUtilisateurResponseDTOs(utilisateurRepository.findAll());
    }

    @Override
    public ResponseEntity<String> deleteUtilisateurById(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur not found"));
        if (utilisateur.getId().equals(currentUserService.getCurrentUserId())) {
            throw new IllegalStateException("Vous ne pouvez pas supprimer votre propre compte");
        }
        if (utilisateur.getRole() == Roles.ADMIN && utilisateurRepository.findByRole(Roles.ADMIN).size() <= 1) {
            throw new IllegalStateException("Impossible de supprimer le dernier administrateur");
        }
        utilisateurRepository.delete(utilisateur);
        // flush pour que l'éventuelle violation de contrainte (données liées) soit levée ici et renvoyée en 409
        utilisateurRepository.flush();
        return ResponseEntity.ok("Utilisateur deleted successfully");
    }

    @Override
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    @Override
    public List<Contact> findAllContacts() {
        return toContactsExcludingCurrentUser(utilisateurRepository.findAll());
    }

    @Override
    public List<Contact> findAllEntrepriseContacts(Long entrpriseId) {
        if (!utilisateurRepository.existsById(entrpriseId)) {
            throw new RessourceNotFoundException("Entreprise not found");
        }
        return toContactsExcludingCurrentUser(utilisateurRepository.findEntrepriseContacts(entrpriseId, Roles.ADMIN));
    }

    @Override
    public List<Contact> findAllCandidatContact(Long etudiantId) {
        if (!utilisateurRepository.existsById(etudiantId)) {
            throw new RessourceNotFoundException("Etudiant not found");
        }
        return toContactsExcludingCurrentUser(utilisateurRepository.findEtudiantContacts(etudiantId));
    }

    @Override
    public List<Contact> findAllEnseignantContacts(Long enseignantId) {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RessourceNotFoundException("Enseignant not found"));
        return toContactsExcludingCurrentUser(utilisateurRepository.findEnseignantContacts(enseignant.getFiliere(), Roles.ADMIN));
    }

    /** Un utilisateur ne doit pas apparaître dans sa propre liste de contacts. */
    private List<Contact> toContactsExcludingCurrentUser(List<Utilisateur> utilisateurs) {
        Long currentUserId = currentUserService.getCurrentUserId();
        return utilisateurMappeur.utilisateursToContacts(utilisateurs.stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .toList());
    }
}
