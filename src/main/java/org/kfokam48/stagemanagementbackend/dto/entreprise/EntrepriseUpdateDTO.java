package org.kfokam48.stagemanagementbackend.dto.entreprise;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.dto.UtilisateurDTO;

@Data
public class EntrepriseUpdateDTO  extends UtilisateurDTO {
    private String nomEntreprise;
    private String secteurActivite;
    private String siteWeb;
    private String description;
    private String telephone;
    private String adresse;
}
