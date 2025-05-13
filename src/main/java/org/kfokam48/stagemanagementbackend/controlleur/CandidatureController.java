package org.kfokam48.stagemanagementbackend.controlleur;

import org.kfokam48.stagemanagementbackend.dto.CandidatureDTO;
import org.kfokam48.stagemanagementbackend.dto.CandidatureResponseDTO;
import org.kfokam48.stagemanagementbackend.service.CandidatureService;
import org.kfokam48.stagemanagementbackend.service.impl.CandidatureServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidatures") // URL de base du contrôleur
public class CandidatureController {

    private final CandidatureServiceImpl candidatureService;

    public CandidatureController(CandidatureServiceImpl candidatureService) {
        this.candidatureService = candidatureService;
    }


    // ✅ Récupérer une candidature par ID
    @GetMapping("/{id}")
    public ResponseEntity<CandidatureResponseDTO> getCandidatureById(@PathVariable Long id) {
        CandidatureResponseDTO candidature = candidatureService.getCandidatureById(id);
        return ResponseEntity.ok(candidature);
    }

    // ✅ Récupérer toutes les candidatures
    @GetMapping
    public ResponseEntity<List<CandidatureResponseDTO>> getAllCandidatures() {
        List<CandidatureResponseDTO> candidatures = candidatureService.getAllCandidatures();
        return ResponseEntity.ok(candidatures);
    }

    // ✅ Ajouter une nouvelle candidature
    @PostMapping
    public ResponseEntity<CandidatureResponseDTO> addCandidature(@RequestBody CandidatureDTO candidatureDTO) {
        CandidatureResponseDTO candidature = candidatureService.addCandidature(candidatureDTO);
        return ResponseEntity.ok(candidature);
    }

    // ✅ Mettre à jour une candidature
    @PutMapping("/{id}")
    public ResponseEntity<CandidatureResponseDTO> updateCandidature(
            @PathVariable Long id,
            @RequestBody CandidatureDTO candidatureDTO) {
        CandidatureResponseDTO candidature = candidatureService.updateCandidature(id, candidatureDTO);
        return ResponseEntity.ok(candidature);
    }

    // ✅ Supprimer une candidature
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCandidature(@PathVariable Long id) {
        return candidatureService.deleteCandidature(id);
    }
}
