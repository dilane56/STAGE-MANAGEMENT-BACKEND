package org.kfokam48.stagemanagementbackend.controlleur;


import org.kfokam48.stagemanagementbackend.dto.entreprise.EntrepriseDTO;
import org.kfokam48.stagemanagementbackend.dto.entreprise.EntrepriseReponseDTO;
import org.kfokam48.stagemanagementbackend.dto.entreprise.EntrepriseUpdateDTO;
import org.kfokam48.stagemanagementbackend.service.EntrepriseService;
import org.kfokam48.stagemanagementbackend.service.impl.EntrepriseServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entreprises") // URL de base du contrôleur
public class EntrepriseController {

    private final EntrepriseServiceImpl entrepriseService;

    public EntrepriseController(EntrepriseServiceImpl entrepriseService) {
        this.entrepriseService = entrepriseService;
    }


    // ✅ Récupérer une entreprise par ID
    @GetMapping("/{id}")
    public ResponseEntity<EntrepriseReponseDTO> getEntrepriseById(@PathVariable Long id) {
        EntrepriseReponseDTO entreprise = entrepriseService.getEntrepriseById(id);
        return ResponseEntity.ok(entreprise);
    }

    // ✅ Créer une entreprise
    @PostMapping
    public ResponseEntity<EntrepriseReponseDTO> creerEntreprise(@RequestBody EntrepriseDTO entrepriseDTO) {
        EntrepriseReponseDTO entreprise = entrepriseService.creerEntreprise(entrepriseDTO);
        return ResponseEntity.ok(entreprise);
    }

    // ✅ Modifier une entreprise
    @PutMapping("/{id}")
    public ResponseEntity<EntrepriseReponseDTO> modifierEntreprise(
            @PathVariable Long id,
            @RequestBody EntrepriseUpdateDTO entrepriseUpdateDTO) {
        EntrepriseReponseDTO entreprise = entrepriseService.modifierEntreprise(id, entrepriseUpdateDTO);
        return ResponseEntity.ok(entreprise);
    }

    // ✅ Supprimer une entreprise
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerEntreprise(@PathVariable Long id) {
        return entrepriseService.supprimerEntreprise(id);
    }

    // ✅ Récupérer toutes les entreprises
    @GetMapping
    public ResponseEntity<List<EntrepriseReponseDTO>> getAllEntreprises() {
        List<EntrepriseReponseDTO> entreprises = entrepriseService.getAllEntreprises();
        return ResponseEntity.ok(entreprises);
    }
}
