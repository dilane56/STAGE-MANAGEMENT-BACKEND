package org.kfokam48.stagemanagementbackend.dto.etudiant;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.dto.utilisateur.UtilisateurDTO;

@Data
public class EtudiantDTO extends UtilisateurDTO {
    private String filiere;
    private String niveau;
    private String anneeScolaire;
}
