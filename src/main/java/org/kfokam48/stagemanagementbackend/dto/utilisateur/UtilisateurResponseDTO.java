package org.kfokam48.stagemanagementbackend.dto.utilisateur;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.enums.Roles;

@Data
public class UtilisateurResponseDTO {
    public Long id;
    public String email;
    private String password;
    private Roles role;
    private String adresse;
    private String telephone;
    private String username;
}
