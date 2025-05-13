package org.kfokam48.stagemanagementbackend.dto.entreprise;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.dto.UtilisateurDTO;
@Data
public class EntrepriseDTO extends UtilisateurDTO {
    private String nomEntreprise;
}
