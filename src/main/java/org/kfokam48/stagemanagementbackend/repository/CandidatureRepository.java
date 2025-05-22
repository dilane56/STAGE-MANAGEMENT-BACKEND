package org.kfokam48.stagemanagementbackend.repository;

import org.kfokam48.stagemanagementbackend.model.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    // Vérifie si l'étudiant a déjà postulé à cette offre
    boolean existsByEtudiantIdAndOffreStageId(Long etudiantId, Long offreStageId);
}
