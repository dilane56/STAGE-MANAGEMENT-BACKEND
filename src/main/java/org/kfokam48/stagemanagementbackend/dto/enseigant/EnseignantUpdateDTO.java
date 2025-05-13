package org.kfokam48.stagemanagementbackend.dto.enseigant;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.dto.UtilisateurDTO;
@Data
public class EnseignantUpdateDTO extends UtilisateurDTO {
    private String specialite;
    private String nom;
    private String prenom;
    private String adresse;
    private String telephone;
}
