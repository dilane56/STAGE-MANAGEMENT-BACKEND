package org.kfokam48.stagemanagementbackend.controlleur;

import org.kfokam48.stagemanagementbackend.model.auth.CustomUserDetails;
import org.kfokam48.stagemanagementbackend.service.StatsService;
import org.kfokam48.stagemanagementbackend.service.auth.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;
    private final CurrentUserService currentUserService;

    public StatsController(StatsService statsService, CurrentUserService currentUserService) {
        this.statsService = statsService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        // L'utilisateur est déjà chargé par le filtre JWT : plus de requête supplémentaire ni de RuntimeException
        CustomUserDetails user = currentUserService.getCurrentUser();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        Object stats = switch (user.getRole()) {
            case ETUDIANT -> statsService.getEtudiantStats(user.getId());
            case ENTREPRISE -> statsService.getEntrepriseStats(user.getId());
            case ENSEIGNANT -> statsService.getEnseignantStats(user.getId());
            case ADMIN -> statsService.getAdminStats();
        };

        response.put("data", stats);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly-evolution")
    public ResponseEntity<Map<String, Object>> getMonthlyEvolution() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", statsService.getMonthlyEvolution());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/internships-by-sector")
    public ResponseEntity<Map<String, Object>> getInternshipsBySector() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", statsService.getInternshipsBySector());
        return ResponseEntity.ok(response);
    }
}
