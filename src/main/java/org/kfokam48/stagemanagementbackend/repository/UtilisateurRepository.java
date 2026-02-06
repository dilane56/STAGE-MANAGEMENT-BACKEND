package org.kfokam48.stagemanagementbackend.repository;

import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.enums.UserStatus;
import org.kfokam48.stagemanagementbackend.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Utilisateur> findByStatus(UserStatus status);
    
    // Contacts pour entreprise : étudiants qui ont postulé + admins
    @Query("SELECT DISTINCT u FROM Utilisateur u WHERE u.role = :adminRole OR u.id IN (SELECT c.etudiant.id FROM Candidature c WHERE c.offreStage.entreprise.id = :entrepriseId)")
    List<Utilisateur> findEntrepriseContacts(@Param("entrepriseId") Long entrepriseId, @Param("adminRole") Roles adminRole);
    
    // Contacts pour étudiant : entreprises auxquelles il a postulé
    @Query("SELECT DISTINCT u FROM Utilisateur u WHERE u.id IN (SELECT c.offreStage.entreprise.id FROM Candidature c WHERE c.etudiant.id = :etudiantId)")
    List<Utilisateur> findEtudiantContacts(@Param("etudiantId") Long etudiantId);
}
