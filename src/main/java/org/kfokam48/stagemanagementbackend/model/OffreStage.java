package org.kfokam48.stagemanagementbackend.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
    private int dureeStage;
    @ElementCollection
    @CollectionTable(name = "offre_competences", joinColumns = @JoinColumn(name = "offre_id"))
    @Column(name = "competence")
    private List<String> competences=new ArrayList<>();
    private LocalDate datePublication;
    private LocalDateTime dateLimiteCandidature;
    private int nombrePlaces;

    @ManyToOne
    @JoinColumn(name = "secteur_id")
    private Secteur secteur;
    
    private LocalDate dateDebutStage;


    @ManyToOne
  //  @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    // Exclus de toString/equals/hashCode : évite une récursion infinie OffreStage <-> Candidature
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "offreStage")
    private List<Candidature> candidatures = new ArrayList<>();
}

