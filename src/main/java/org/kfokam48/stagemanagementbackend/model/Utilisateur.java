package org.kfokam48.stagemanagementbackend.model;
import jakarta.persistence.*;
import lombok.Data;
import org.kfokam48.stagemanagementbackend.enums.Roles;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // Héritage pour les différents types d'utilisateurs
public abstract class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    private String username;
    private String telephone;
    private String adresse;

    @Enumerated(EnumType.STRING)
    private Roles role; // Enumération des rôles
}
