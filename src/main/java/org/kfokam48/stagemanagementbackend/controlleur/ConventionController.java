package org.kfokam48.stagemanagementbackend.controlleur;
import org.kfokam48.stagemanagementbackend.dto.convention.ConventionRequestDTO;
import org.kfokam48.stagemanagementbackend.dto.convention.ConventionResponseDTO;
import org.kfokam48.stagemanagementbackend.service.ConventionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Les contrôles d'appartenance et de cycle de vie sont faits dans ConventionServiceImpl.
 */
@RestController
@RequestMapping("/api/conventions") // URL de base du contrôleur
public class ConventionController {

    private final ConventionService conventionService;

    public ConventionController(ConventionService conventionService) {
        this.conventionService = conventionService;
    }

    // ✅ Récupérer une convention par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ENTREPRISE') or hasRole('ADMIN') or hasRole('ENSEIGNANT') or hasRole('ETUDIANT')")
    public ResponseEntity<ConventionResponseDTO> findByConventionId(@PathVariable Long id) {
        ConventionResponseDTO convention = conventionService.findByConventionId(id);
        return ResponseEntity.ok(convention);
    }

    // ✅ Ajouter une nouvelle convention
    @PostMapping
    @PreAuthorize("hasRole('ENTREPRISE') or hasRole('ADMIN')")
    public ResponseEntity<ConventionResponseDTO> createConvention(
            @RequestBody ConventionRequestDTO conventionRequestDTO) {
        return ResponseEntity.ok(conventionService.createConvention(conventionRequestDTO));
    }

    /**
     * ✅ Mettre à jour une convention.
     * La candidature peut être passée en paramètre "candidature-id" (ancien contrat) ou dans le corps
     * JSON {"idCandidature": ...} (format envoyé par le frontend, qui provoquait auparavant une erreur 400).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ENTREPRISE') or hasRole('ADMIN')")
    public ResponseEntity<ConventionResponseDTO> updateConvention(
            @PathVariable Long id,
            @RequestParam(value = "candidature-id", required = false) Long idCandidature,
            @RequestBody(required = false) ConventionRequestDTO body) {
        Long candidatureId = idCandidature != null ? idCandidature : (body != null ? body.getIdCandidature() : null);
        ConventionResponseDTO convention = conventionService.updateConvention(id, candidatureId);
        return ResponseEntity.ok(convention);
    }

    // ✅ Supprimer une convention (admin, ou entreprise propriétaire tant qu'elle n'est pas validée)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENTREPRISE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteConvention(@PathVariable Long id) {
        String message = conventionService.deleteConvention(id);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // ✅ Récupérer toutes les conventions
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConventionResponseDTO>> findAllConventions() {
        List<ConventionResponseDTO> conventions = conventionService.findAllConventions();
        return ResponseEntity.ok(conventions);
    }

    @GetMapping("/entreprise/{entrepriseId}")
    @PreAuthorize("hasRole('ENTREPRISE') or hasRole('ADMIN')")
    public ResponseEntity<List<ConventionResponseDTO>> findConventionsByEntreprise(@PathVariable Long entrepriseId) {
        List<ConventionResponseDTO> conventions = conventionService.findConventionsByEntreprise(entrepriseId);
        return ResponseEntity.ok(conventions);
    }

    @GetMapping("/enseignant/{enseignantId}")
    @PreAuthorize("hasRole('ENSEIGNANT') or hasRole('ADMIN')")
    public ResponseEntity<List<ConventionResponseDTO>> findConventionsByEnseignant(@PathVariable Long enseignantId) {
        List<ConventionResponseDTO> conventions = conventionService.findConventionsByEnseignant(enseignantId);
        return ResponseEntity.ok(conventions);
    }

    // ✅ Valider une convention par un enseignant
    @PutMapping("/{id}/valider-enseignant/{enseignantId}")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ConventionResponseDTO> validateConventionByEnseignant(
            @PathVariable Long id, @PathVariable Long enseignantId) {

        ConventionResponseDTO convention = conventionService.validateConventionByEnseignant(enseignantId, id);
        return ResponseEntity.ok(convention);
    }

    // ✅ Approuver une convention par un administrateur
    @PutMapping("/{id}/approuver-administrateur/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConventionResponseDTO> approuveConventionByAdministrator(
            @PathVariable Long id, @PathVariable Long adminId) {

        ConventionResponseDTO convention = conventionService.approuveConventionByAdministrator(adminId, id);
        return ResponseEntity.ok(convention);
    }



    @GetMapping("/{id}/generate-pdf")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENSEIGNANT') or hasRole('ENTREPRISE') or hasRole('ETUDIANT')")
    public ResponseEntity<byte[]> generateConventionPdf(@PathVariable Long id) throws Exception {
        return conventionService.generateConventionPdf(id);
    }

}
