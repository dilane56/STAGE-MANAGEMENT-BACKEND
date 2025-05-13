package org.kfokam48.stagemanagementbackend.controlleur;



import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantDTO;
import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantResponseDTO;
import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantUpdateDTO;
import org.kfokam48.stagemanagementbackend.service.Etudiantservice;
import org.kfokam48.stagemanagementbackend.service.impl.EtudiantserviceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants") // URL de base du contrôleur
public class EtudiantController {

    private final EtudiantserviceImpl etudiantService;

    public EtudiantController(EtudiantserviceImpl etudiantService) {
        this.etudiantService = etudiantService;
    }


    // ✅ Récupérer un étudiant par ID
    @GetMapping("/{id}")
    public ResponseEntity<EtudiantResponseDTO> getEtudiantById(@PathVariable Long id) {
        EtudiantResponseDTO etudiant = etudiantService.getEtudiantById(id);
        return ResponseEntity.ok(etudiant);
    }

    // ✅ Créer un étudiant
    @PostMapping
    public ResponseEntity<EtudiantResponseDTO> createEtudiant(@RequestBody EtudiantDTO etudiantDTO) {
        EtudiantResponseDTO etudiant = etudiantService.createEtudiant(etudiantDTO);
        return ResponseEntity.ok(etudiant);
    }

    // ✅ Mettre à jour un étudiant
    @PutMapping("/{id}")
    public ResponseEntity<EtudiantResponseDTO> updateEtudiant(
            @PathVariable Long id,
            @RequestBody EtudiantUpdateDTO etudiantUpdateDTO) {
        EtudiantResponseDTO etudiant = etudiantService.updateEtudiant(id, etudiantUpdateDTO);
        return ResponseEntity.ok(etudiant);
    }

    // ✅ Supprimer un étudiant
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEtudiant(@PathVariable Long id) {
        return etudiantService.deleteEtudiant(id);
    }

    // ✅ Récupérer tous les étudiants
    @GetMapping
    public ResponseEntity<List<EtudiantResponseDTO>> getAllEtudiants() {
        List<EtudiantResponseDTO> etudiants = etudiantService.getAllEtudiants();
        return ResponseEntity.ok(etudiants);
    }
}
