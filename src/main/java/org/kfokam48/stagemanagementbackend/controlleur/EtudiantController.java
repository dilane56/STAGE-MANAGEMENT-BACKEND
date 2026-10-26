package org.kfokam48.stagemanagementbackend.controlleur;



import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantDTO;
import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantResponseDTO;
import org.kfokam48.stagemanagementbackend.service.impl.EtudiantserviceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants") // URL de base du contrôleur
public class EtudiantController {

    private final EtudiantserviceImpl etudiantService;

    public EtudiantController(EtudiantserviceImpl etudiantService) {
        this.etudiantService = etudiantService;
    }


    // ✅ Récupérer un étudiant par ID (l'étudiant lui-même ou un admin)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ETUDIANT') and #id == authentication.principal.id)")
    public ResponseEntity<EtudiantResponseDTO> getEtudiantById(@PathVariable Long id) {
        EtudiantResponseDTO etudiant = etudiantService.getEtudiantById(id);
        return ResponseEntity.ok(etudiant);
    }

    // ✅ Créer un étudiant (réservé à l'administrateur)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EtudiantResponseDTO> createEtudiant(@RequestBody EtudiantDTO etudiantDTO) {
        EtudiantResponseDTO etudiant = etudiantService.createEtudiant(etudiantDTO);
        return ResponseEntity.ok(etudiant);
    }

    // ✅ Mettre à jour un étudiant (l'étudiant lui-même ou un admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ETUDIANT') and #id == authentication.principal.id)")
    public ResponseEntity<EtudiantResponseDTO> updateEtudiant(
            @PathVariable Long id,
            @RequestBody EtudiantDTO etudiantDTO) {
        EtudiantResponseDTO etudiant = etudiantService.updateEtudiant(id, etudiantDTO);
        return ResponseEntity.ok(etudiant);
    }

    // ✅ Supprimer un étudiant
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteEtudiant(@PathVariable Long id) {
        return etudiantService.deleteEtudiant(id);
    }

    // ✅ Récupérer tous les étudiants
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EtudiantResponseDTO>> getAllEtudiants() {
        List<EtudiantResponseDTO> etudiants = etudiantService.getAllEtudiants();
        return ResponseEntity.ok(etudiants);
    }

    // ✅ Étudiants de la filière d'un enseignant (l'enseignant lui-même ou un admin)
    @GetMapping("/enseignant/{enseignantId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ENSEIGNANT') and #enseignantId == authentication.principal.id)")
    public ResponseEntity<List<EtudiantResponseDTO>> getEtudiantsByEnseignant(@PathVariable Long enseignantId) {
        List<EtudiantResponseDTO> etudiants = etudiantService.getEtudiantsByEnseignant(enseignantId);
        return ResponseEntity.ok(etudiants);
    }
}
