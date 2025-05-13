package org.kfokam48.stagemanagementbackend.dto.entreprise;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.dto.OffreStageInEntrepriseDTO;
import org.kfokam48.stagemanagementbackend.dto.UtilisateurResponseDTO;
import org.kfokam48.stagemanagementbackend.model.OffreStage;

import java.util.List;
@Data
public class EntrepriseReponseDTO extends UtilisateurResponseDTO {
    private String nomEntreprise;
    private String secteurActivite;
    private String siteWeb;
    private String description;
    private List<OffreStageInEntrepriseDTO> offres;
}
