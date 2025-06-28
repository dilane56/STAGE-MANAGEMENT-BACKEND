package org.kfokam48.stagemanagementbackend.dto.offreStage;

import lombok.Data;

import java.util.List;

@Data
public class OffreStageResponseDTO {
   private Long id;
    private String intitule;
    private String description;
    private String duree;
    private String nomEntreprise;
    private String localisation;
    private List<CandidatureInOffreStageDTO> candidatures;
    private List<String> competences;
    private String secteurName;



}
