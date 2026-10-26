package org.kfokam48.stagemanagementbackend.controlleur;

import io.swagger.v3.oas.annotations.Operation;
import org.kfokam48.stagemanagementbackend.dto.candidature.CandidatureDTO;
import org.kfokam48.stagemanagementbackend.dto.candidature.CandidatureResponseDTO;
import org.kfokam48.stagemanagementbackend.service.impl.CandidatureServiceImpl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Les contrôles d'appartenance (un étudiant ne voit que ses candidatures, une entreprise
 * que celles reçues sur ses offres) sont faits dans CandidatureServiceImpl.
 */
@RestController
@RequestMapping("/api/candidatures") // URL de base du contrôleur
public class CandidatureController {

    private final CandidatureServiceImpl candidatureService;

    public CandidatureController(CandidatureServiceImpl candidatureService) {
        this.candidatureService = candidatureService;
    }


    // ✅ Récupérer une candidature par ID
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ENTREPRISE') or hasRole('ENSEIGNANT') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CandidatureResponseDTO> getCandidatureById(@PathVariable Long id) {
        CandidatureResponseDTO candidature = candidatureService.getCandidatureById(id);
        return ResponseEntity.ok(candidature);
    }

    // ✅ Récupérer toutes les candidatures
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CandidatureResponseDTO>> getAllCandidatures() {
        List<CandidatureResponseDTO> candidatures = candidatureService.getAllCandidatures();
        return ResponseEntity.ok(candidatures);
    }

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<List<CandidatureResponseDTO>> getCandidaturesByEtudiant(@PathVariable Long etudiantId) {
        List<CandidatureResponseDTO> candidatures = candidatureService.getCandidaturesByEtudiant(etudiantId);
        return ResponseEntity.ok(candidatures);
    }

    @GetMapping("/entreprise/{entrepriseId}")
    @PreAuthorize("hasRole('ENTREPRISE') or hasRole('ADMIN')")
    public ResponseEntity<List<CandidatureResponseDTO>> getCandidaturesByEntreprise(@PathVariable Long entrepriseId) {
        List<CandidatureResponseDTO> candidatures = candidatureService.getCandidaturesByEntreprise(entrepriseId);
        return ResponseEntity.ok(candidatures);
    }

    // ✅ Ajouter une nouvelle candidature (CV PDF obligatoire)
    @PostMapping(value = "/ajouter", consumes = "multipart/form-data")
    @Operation(summary = "Ajouter une candidature avec CV et Lettre de motivation")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<CandidatureResponseDTO> addCandidature(
            @RequestParam Long idEtudiant,
            @RequestParam Long idOffre,
            @RequestParam("cv") MultipartFile file,
            @RequestParam("lettreMotivation") String lettreMotivation
    ) throws Exception {

        CandidatureDTO candidatureDTO = new CandidatureDTO();
        candidatureDTO.setEtudiantId(idEtudiant);
        candidatureDTO.setOffreStageId(idOffre);
        candidatureDTO.setLettreMotivation(lettreMotivation);

        CandidatureResponseDTO candidatureResponseDTO = candidatureService.addCandidature(candidatureDTO, file);

        return ResponseEntity.ok(candidatureResponseDTO);
    }


    // ✅ Mettre à jour une candidature avec fichier (CV optionnel)
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<CandidatureResponseDTO> updateCandidature(
            @PathVariable Long id,
            @RequestParam(required = false) Long idEtudiant,
            @RequestParam(required = false) Long idOffre,
            @RequestParam String lettreMotivation,
            @RequestParam(value = "cv", required = false) MultipartFile file) throws Exception {
        CandidatureResponseDTO candidature = candidatureService.updateCandidature(id, idEtudiant, idOffre, lettreMotivation, file);
        return ResponseEntity.ok(candidature);
    }


    // ✅ Supprimer une candidature
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteCandidature(@PathVariable Long id) {
        return candidatureService.deleteCandidature(id);
    }


    // ✅ Télécharger le CV (auparavant accessible à tout utilisateur connecté, via une URL stockée)
    @GetMapping("/downloadCV/{id}/cv")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ENTREPRISE') or hasRole('ENSEIGNANT') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> getCv(@PathVariable Long id) throws Exception {
        byte[] fileBytes = candidatureService.downloadCv(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("cv_candidature_" + id + ".pdf").build());

        return ResponseEntity.ok().headers(headers).body(fileBytes);
    }

    @PutMapping("/{id}/statut")
    @PreAuthorize("hasRole('ENTREPRISE') or hasRole('ADMIN')")
    public ResponseEntity<String> updateCandidatureStatut(
            @PathVariable Long id,
            @RequestParam String statut,
            @RequestParam(required = false) String message) {

        return candidatureService.updateCandidatureStatut(id, statut, message);
    }
}
