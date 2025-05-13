package org.kfokam48.stagemanagementbackend.dto.administrateur;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.enums.Roles;

@Data
public class AdministrateurUpdateDTO  {
    private String email;
    private String password;
    private String username;
    private String telephone;
    private String adresse;
    private Roles role;
}
