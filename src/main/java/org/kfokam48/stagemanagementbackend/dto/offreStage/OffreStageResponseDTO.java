package org.kfokam48.stagemanagementbackend.dto.offreStage;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OffreStageResponseDTO {
   private Long id;
    private String intitule;
    private String description;
    private int dureeStage;
    private String nomEntreprise;
    /** Ajouté : permet au frontend d'identifier l'entreprise propriétaire. */
    private Long entrepriseId;
    private String localisation;
    /** Liste des candidatures : renseignée uniquement pour l'entreprise propriétaire et l'administrateur. */
    private List<CandidatureInOffreStageDTO> candidatures;
    private List<String> competences;
    private String secteurName;
    /** Ajouté : nécessaire pour pré-remplir le formulaire de modification d'une offre. */
    private Long secteurId;
    private LocalDate datePublication;
    private LocalDateTime dateLimiteCandidature;
    private LocalDate dateDebutStage;
    private int nombrePlaces;



}
