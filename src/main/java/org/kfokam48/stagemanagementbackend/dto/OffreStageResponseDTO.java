package org.kfokam48.stagemanagementbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class OffreStageResponseDTO {
   private Long id;
    private String intitule;
    private String description;
    private String domaine;
    private String duree; // en mois
    private String nomEntreprise; // L'entreprise qui publie l'offre
    private String localisation;
    private List<CandidatureInOffreStageDTO> candidatures; // Liste des candidatures associées à l'offre


}
