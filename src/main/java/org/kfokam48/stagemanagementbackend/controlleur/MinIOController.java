package org.kfokam48.stagemanagementbackend.controlleur;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/minio")
public class MinIOController {

    // Simple sonde de disponibilité de l'API, réservée à l'administrateur
    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testConnection() {
        return ResponseEntity.ok("MinIO est bien connecté !");
    }
}
