package org.kfokam48.stagemanagementbackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ConventionStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "candidature_id", nullable = false)
    private Candidature candidature; // Liée à une candidature acceptée

    @ManyToOne
    @JoinColumn(name = "enseigant_id")
    private Enseignant enseignantValideur; // Enseignant qui valide la convention

    @ManyToOne
    @JoinColumn(name = "administrateur_id")
    private Administrateur aprouvalAdministrator;

    private String pdfConventionPath; // Chemin du fichier PDF stocké (S3/MinIO)

    private boolean signatureElectronique;
}

