package org.kfokam48.stagemanagementbackend.repository;

import org.kfokam48.stagemanagementbackend.model.OffreStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OffreStageRepository extends JpaRepository<OffreStage, Long> {
    // Custom query methods can be defined here if needed
    // For example, to find all offers by a specific company:
    // List<OffreStage> findByEntrepriseId(Long entrepriseId);
}
