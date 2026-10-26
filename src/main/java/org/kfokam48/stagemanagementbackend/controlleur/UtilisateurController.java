package org.kfokam48.stagemanagementbackend.controlleur;


import org.kfokam48.stagemanagementbackend.dto.utilisateur.Contact;
import org.kfokam48.stagemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.stagemanagementbackend.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs") // Définition de l'URL de base du contrôleur
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // ✅ Récupérer un utilisateur par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> getUtilisateurById(@PathVariable Long id) {
        UtilisateurResponseDTO utilisateur = utilisateurService.getUtilisateurById(id);
        return ResponseEntity.ok(utilisateur);
    }

    // ✅ Récupérer un utilisateur par email (son propre compte, ou n'importe lequel pour un admin)
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.principal.username")
    public ResponseEntity<UtilisateurResponseDTO> getUtilisateurByEmail(@PathVariable String email) {
        UtilisateurResponseDTO utilisateur = utilisateurService.getUtilisateurByEmail(email);
        return ResponseEntity.ok(utilisateur);
    }

    // ✅ Récupérer la liste des utilisateurs
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UtilisateurResponseDTO>> getAllUtilisateurs() {
        List<UtilisateurResponseDTO> utilisateurs = utilisateurService.getAllUtilisateurs();
        return ResponseEntity.ok(utilisateurs);
    }

    // ✅ Supprimer un utilisateur par ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUtilisateurById(@PathVariable Long id) {
        return utilisateurService.deleteUtilisateurById(id);
    }

    // ✅ Vérifier si un email existe
    @GetMapping("/exists/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(utilisateurService.existsByEmail(email));
    }

    // ✅ Tous les contacts (messagerie de l'administrateur)
    @GetMapping("/contacts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Contact>> getAllContacts() {
        List<Contact> contacts = utilisateurService.findAllContacts();
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    @GetMapping("/contacts/entreprise/{id}")
    @PreAuthorize("hasRole('ENTREPRISE') and #id == authentication.principal.id")
    public ResponseEntity<List<Contact>> getEntrepriseContacts( @PathVariable Long id) {
        List<Contact> contacts = utilisateurService.findAllEntrepriseContacts(id);
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    @GetMapping("/contacts/candidat/{id}")
    @PreAuthorize("hasRole('ETUDIANT') and #id == authentication.principal.id")
    public ResponseEntity<List<Contact>> getCandidatContacts(@PathVariable Long id) {
        List<Contact> contacts = utilisateurService.findAllCandidatContact(id);
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    // ✅ Nouveau : contacts d'un enseignant (étudiants de sa filière + administrateurs).
    // Le frontend appelait déjà cet endpoint qui n'existait pas (erreur 404 dans la messagerie enseignant).
    @GetMapping("/contacts/enseignant/{id}")
    @PreAuthorize("hasRole('ENSEIGNANT') and #id == authentication.principal.id")
    public ResponseEntity<List<Contact>> getEnseignantContacts(@PathVariable Long id) {
        List<Contact> contacts = utilisateurService.findAllEnseignantContacts(id);
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }
}
