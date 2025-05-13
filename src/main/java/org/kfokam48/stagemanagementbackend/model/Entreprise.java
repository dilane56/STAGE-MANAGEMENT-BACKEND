package org.kfokam48.stagemanagementbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@PrimaryKeyJoinColumn(name = "utilisateur_id")
public class Entreprise extends Utilisateur {
    private String nomEntreprise;
    private String secteurActivite;
    private String siteWeb;
    private String description;

    @OneToMany(mappedBy = "entreprise")
    private List<OffreStage> offres =new ArrayList<>();
}


