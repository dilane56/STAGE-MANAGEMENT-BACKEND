package org.kfokam48.stagemanagementbackend.model;


import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class OffreStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String intitule;
    private String description;
    private String domaine;
    private String localisation;
    private String duree; // en mois

    @ManyToOne
  //  @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise; // L'entreprise qui publie l'offre

    @OneToMany(mappedBy = "offreStage")
    private List<Candidature> candidatures = new ArrayList<>();
}

