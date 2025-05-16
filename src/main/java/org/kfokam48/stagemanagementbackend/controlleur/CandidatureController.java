package org.kfokam48.stagemanagementbackend.controlleur;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.kfokam48.stagemanagementbackend.dto.CandidatureDTO;
import org.kfokam48.stagemanagementbackend.dto.CandidatureResponseDTO;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.model.Candidature;
import org.kfokam48.stagemanagementbackend.repository.CandidatureRepository;
import org.kfokam48.stagemanagementbackend.service.impl.CandidatureServiceImpl;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/candidatures") // URL de base du contrôleur
public class CandidatureController {

    private final CandidatureServiceImpl candidatureService;
    private final CandidatureRepository candidatureRepository;

    public CandidatureController(CandidatureServiceImpl candidatureService, CandidatureRepository candidatureRepository) {
        this.candidatureService = candidatureService;
        this.candidatureRepository = candidatureRepository;
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
    @PostMapping(value = "/ajouter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter une candidature avec CV")
    public ResponseEntity<CandidatureResponseDTO> addCandidature(
            @RequestPart("candidature") CandidatureDTO candidatureDTO,
            @RequestParam("file") MultipartFile file)  {
        try {


            CandidatureResponseDTO candidature = candidatureService.addCandidature(candidatureDTO, file);
            return ResponseEntity.ok(candidature);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur de traitement des fichiers");
        }
    }

    @PostMapping()
    @Operation(summary = "Ajouter une candidature sans  upload de CV")
    public ResponseEntity<CandidatureResponseDTO> addCandidature(@RequestBody CandidatureDTO candidatureDTO) {
        return ResponseEntity.ok(candidatureService.addCandidature(candidatureDTO));
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

    @PostMapping(value = "/upload-cv", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadCv(@RequestParam("file") MultipartFile file) {
        try {
            // Définir le chemin du fichier
            String filePath = "uploads/" + file.getOriginalFilename();
            File dest = new File(filePath);
            file.transferTo(dest); // Sauvegarde du fichier dans le serveur

            return ResponseEntity.ok(filePath); // Retourne le chemin du fichier
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload du CV : " + e.getMessage());
        }

    }

    @GetMapping("/cv/{id}")
    public ResponseEntity<Resource> getCv(@PathVariable Long id) throws IOException {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Candidature not found"));

        File file = new File(candidature.getCvPath());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body((Resource) new InputStreamResource(new FileInputStream(file)));
    }


    @PostMapping(value = "/add-cv", consumes = "multipart/form-data")
    @Operation(summary = "Ajouter CV a une candidature")
    public ResponseEntity<String> uploadCv(@RequestParam("file") MultipartFile file, @RequestParam Long id) throws Exception {
        return candidatureService.uploadImage(file, id);

    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<String> updateCandidatureStatut(
            @PathVariable Long id,
            @RequestParam String statut) throws IOException {

        return candidatureService.updateCandidatureStatut(id, statut);
    }
}
