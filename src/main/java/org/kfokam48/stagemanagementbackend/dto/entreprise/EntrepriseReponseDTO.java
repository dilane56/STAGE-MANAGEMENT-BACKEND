package org.kfokam48.stagemanagementbackend.dto.entreprise;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.dto.offreStage.OffreStageInEntrepriseDTO;
import org.kfokam48.stagemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;

import java.util.List;
@Data
public class EntrepriseReponseDTO extends UtilisateurResponseDTO {
    private String nomEntreprise;
    private String secteurActivite;
    private String siteWeb;
    private String description;
    private List<OffreStageInEntrepriseDTO> offres;
}
