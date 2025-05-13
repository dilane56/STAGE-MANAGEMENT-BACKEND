package org.kfokam48.stagemanagementbackend.dto.enseigant;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.dto.UtilisateurResponseDTO;
@Data
public class EnseignantResponseDTO extends UtilisateurResponseDTO {
    private String specialite;
    private String nom;
    private String prenom;

}
