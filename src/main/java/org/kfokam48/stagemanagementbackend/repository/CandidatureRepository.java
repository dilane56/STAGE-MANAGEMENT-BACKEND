package org.kfokam48.stagemanagementbackend.repository;

import org.kfokam48.stagemanagementbackend.enums.StatutCandidature;
import org.kfokam48.stagemanagementbackend.model.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    boolean existsByEtudiantIdAndOffreStageId(Long etudiantId, Long offreStageId);
    List<Candidature> findByEtudiantId(Long etudiantId);
    List<Candidature> findByOffreStageEntrepriseId(Long entrepriseId);
    Long countByEtudiantId(Long etudiantId);
    Long countByEtudiantIdAndStatut(Long etudiantId, StatutCandidature statut);
    Long countByOffreStageEntrepriseId(Long entrepriseId);
    Long countByOffreStageEntrepriseIdAndStatut(Long entrepriseId, StatutCandidature statut);
    
    @Query("SELECT EXTRACT(MONTH FROM c.dateCandidature) as month, COUNT(c) FROM Candidature c WHERE EXTRACT(YEAR FROM c.dateCandidature) = EXTRACT(YEAR FROM CURRENT_DATE) GROUP BY EXTRACT(MONTH FROM c.dateCandidature) ORDER BY month")
    List<Object[]> countApplicationsByMonth();

}
