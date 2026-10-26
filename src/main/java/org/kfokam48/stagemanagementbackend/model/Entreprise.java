package org.kfokam48.stagemanagementbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@PrimaryKeyJoinColumn(name = "utilisateur_id")
public class Entreprise extends Utilisateur {

    private String domaineActivite;
    private String siteWeb;
    private String description;
    private LocalDate dateCreation;

    // Exclus de toString/equals/hashCode : évite une récursion infinie Entreprise <-> OffreStage
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "entreprise")
    private List<OffreStage> offres =new ArrayList<>();
}


