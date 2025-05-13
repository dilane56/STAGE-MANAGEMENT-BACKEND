package org.kfokam48.stagemanagementbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;

@Data
@Entity
@PrimaryKeyJoinColumn(name = "utilisateur_id")
public class Etudiant extends Utilisateur {
    private String nom;
    private String prenom;
    private String filiere;
    private String anneeScolaire;
    private String niveau;
    private String dateNaissance;

}

