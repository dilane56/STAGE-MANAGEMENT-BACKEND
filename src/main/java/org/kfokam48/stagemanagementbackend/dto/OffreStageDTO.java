package org.kfokam48.stagemanagementbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OffreStageDTO {
    @NotNull(message = "L'intitulé de l'offre de stage est obligatoire")
    @NotBlank(message = "L'intitulé de l'offre de stage ne peut pas être vide")
    private String intitule;
    private String description;
    private String domaine;
    @NotNull(message = "La durée de l'offre de stage est obligatoire")
    private String duree; // en mois
    @NotNull(message = "L'entreprise qui publie l'offre est obligatoire")
    private Long entrepriseId; // L'entreprise qui publie l'offre
    private String localisation;

}
