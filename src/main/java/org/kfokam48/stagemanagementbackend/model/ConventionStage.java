package org.kfokam48.stagemanagementbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.kfokam48.stagemanagementbackend.enums.StatutCandidature;

import java.time.LocalDate;

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
    @JoinColumn(name = "enseignant_id" , nullable = true)
    private Enseignant enseignantValideur; // Enseignant qui valide la convention

    @ManyToOne
    @JoinColumn(name = "administrateur_id", nullable = true)
    private Administrateur aprouvalAdministrator;
    @Column(length = 500) // ✅ Définit une limite de 500 caractères
    private String pdfConventionPath; // Chemin du fichier PDF stocké (S3/MinIO)
    private StatutCandidature statutConvention;
    private LocalDate DateCreation;

    private LocalDate DateValidation;
    private LocalDate DateAprouval;


}

