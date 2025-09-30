package org.kfokam48.stagemanagementbackend.dto.utilisateur;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.enums.UserStatus;

import java.time.Instant;

@Data
public class Contact {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String role;
    private UserStatus status;
    private Instant derniereConnexion;
}
