package org.kfokam48.stagemanagementbackend.dto;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.enums.StatutCandidature;

import java.time.LocalDate;
import java.util.Date;

@Data
public class CandidatureResponseDTO {
    private Long id;
    private String etudiantUsername; // UtilisateurResponseDTO
    private OffreStageInCandidatureDTO offreStage; // OffreStageInCandidatureDTO
    private LocalDate dateCandidature;
    private StatutCandidature statutCandidature;
    private String lettreMotivation;

    // Add any other fields you need for the response
}
