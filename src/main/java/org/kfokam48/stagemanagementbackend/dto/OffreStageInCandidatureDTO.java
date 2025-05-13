package org.kfokam48.stagemanagementbackend.dto;

import lombok.Data;

@Data
public class OffreStageInCandidatureDTO {
    private String intitule;
    private String domaine;
    private String duree; // en mois
    private String nomEntreprise; // L'entreprise qui publie l'offre
}
