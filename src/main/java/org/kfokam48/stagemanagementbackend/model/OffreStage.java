package org.kfokam48.stagemanagementbackend.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private String localisation;
    private int duree;
    private String domaine;
    @ElementCollection
    @CollectionTable(name = "offre_competences", joinColumns = @JoinColumn(name = "offre_id"))
    @Column(name = "competence")
    private List<String> competences=new ArrayList<>();
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalDateTime dateLimiteCandidature;
    @ManyToOne
    @JoinColumn(name = "secteur_id")
    private Secteur secteur;
    

    @ManyToOne
  //  @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @OneToMany(mappedBy = "offreStage")
    private List<Candidature> candidatures = new ArrayList<>();
}

