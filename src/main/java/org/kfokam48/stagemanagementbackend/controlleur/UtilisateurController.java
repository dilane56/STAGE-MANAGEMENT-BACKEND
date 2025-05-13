package org.kfokam48.stagemanagementbackend.controlleur;


import org.kfokam48.stagemanagementbackend.dto.UtilisateurResponseDTO;
import org.kfokam48.stagemanagementbackend.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UtilisateurResponseDTO> getUtilisateurById(@PathVariable Long id) {
        UtilisateurResponseDTO utilisateur = utilisateurService.getUtilisateurById(id);
        return ResponseEntity.ok(utilisateur);
    }

    // ✅ Récupérer un utilisateur par nom d'utilisateur
    @GetMapping("/username/{username}")
    public ResponseEntity<UtilisateurResponseDTO> getUtilisateurByUsername(@PathVariable String username) {
        UtilisateurResponseDTO utilisateur = utilisateurService.getUtilisateurByUsername(username);
        return ResponseEntity.ok(utilisateur);
    }

    // ✅ Récupérer un utilisateur par email
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurResponseDTO> getUtilisateurByEmail(@PathVariable String email) {
        UtilisateurResponseDTO utilisateur = utilisateurService.getUtilisateurByEmail(email);
        return ResponseEntity.ok(utilisateur);
    }

    // ✅ Récupérer la liste des utilisateurs
    @GetMapping
    public ResponseEntity<List<UtilisateurResponseDTO>> getAllUtilisateurs() {
        List<UtilisateurResponseDTO> utilisateurs = utilisateurService.getAllUtilisateurs();
        return ResponseEntity.ok(utilisateurs);
    }

    // ✅ Supprimer un utilisateur par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUtilisateurById(@PathVariable Long id) {
        return utilisateurService.deleteUtilisateurById(id);
    }

    // ✅ Vérifier si un email existe
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(utilisateurService.existsByEmail(email));
    }

    // ✅ Vérifier si un nom d'utilisateur existe
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(utilisateurService.existsByUsername(username));
    }
}