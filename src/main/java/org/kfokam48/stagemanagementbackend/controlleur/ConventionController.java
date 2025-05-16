package org.kfokam48.stagemanagementbackend.controlleur;


import org.kfokam48.stagemanagementbackend.dto.convention.ConventionRequestDTO;
import org.kfokam48.stagemanagementbackend.dto.convention.ConventionResponseDTO;
import org.kfokam48.stagemanagementbackend.service.ConventionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/conventions") // URL de base du contrôleur
public class ConventionController {

    private final ConventionService conventionService;

    public ConventionController(ConventionService conventionService) {
        this.conventionService = conventionService;
    }

    // ✅ Récupérer une convention par ID
    @GetMapping("/{id}")
    public ResponseEntity<ConventionResponseDTO> findByConventionId(@PathVariable Long id) {
        ConventionResponseDTO convention = conventionService.findByConventionId(id);
        return ResponseEntity.ok(convention);
    }

    // ✅ Créer une nouvelle convention
    @PostMapping(value = "/ajouter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConventionResponseDTO> createConvention(
            @RequestPart("convention") ConventionRequestDTO conventionRequestDTO,
            @RequestPart("pdf") MultipartFile file) throws Exception {

        ConventionResponseDTO convention = conventionService.createConvention(conventionRequestDTO, file);
        return ResponseEntity.ok(convention);
    }
    // ✅ Mettre à jour une convention
    @PutMapping
    public ResponseEntity<ConventionResponseDTO> updateConvention(@RequestBody ConventionRequestDTO conventionRequestDTO) {
        ConventionResponseDTO convention = conventionService.updateConvention(conventionRequestDTO);
        return ResponseEntity.ok(convention);
    }

    // ✅ Supprimer une convention
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteConvention(@PathVariable Long id) {
        String message = conventionService.deleteConvention(id);
        return ResponseEntity.ok(message);
    }

    // ✅ Récupérer toutes les conventions
    @GetMapping
    public ResponseEntity<List<ConventionResponseDTO>> findAllConventions() {
        List<ConventionResponseDTO> conventions = conventionService.findAllConventions();
        return ResponseEntity.ok(conventions);
    }

    // ✅ Valider une convention par un enseignant
    @PutMapping("/{id}/valider-enseignant")
    public ResponseEntity<ConventionResponseDTO> validateConventionByEnseignant(@PathVariable Long id) {
        ConventionResponseDTO convention = conventionService.validateConventionByEnseignant(id);
        return ResponseEntity.ok(convention);
    }

    // ✅ Approuver une convention par un administrateur
    @PutMapping("/{id}/approuver-administrateur")
    public ResponseEntity<ConventionResponseDTO> approuveConventionByAdministrator(@PathVariable Long id) {
        ConventionResponseDTO convention = conventionService.approuveConventionByAdministrator(id);
        return ResponseEntity.ok(convention);
    }
}
