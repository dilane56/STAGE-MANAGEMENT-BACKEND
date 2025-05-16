package org.kfokam48.stagemanagementbackend.repository;

import org.kfokam48.stagemanagementbackend.model.OffreStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffreStageRepository extends JpaRepository<OffreStage, Long> {
    // Custom query methods can be defined here if needed
    // For example, to find all offers by a specific company:
    // List<OffreStage> findByEntrepriseId(Long entrepriseId);
    // Filtrer les offres par localisation, durée et domaine
    List<OffreStage> findByLocalisationContainingAndDureeGreaterThanEqualAndDomaineContaining(
            String localisation, String duree, String domaine);

}
