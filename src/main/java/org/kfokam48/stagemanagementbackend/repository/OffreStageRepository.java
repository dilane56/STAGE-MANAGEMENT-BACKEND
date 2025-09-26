package org.kfokam48.stagemanagementbackend.repository;

import org.kfokam48.stagemanagementbackend.model.OffreStage;
import org.kfokam48.stagemanagementbackend.model.Secteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffreStageRepository extends JpaRepository<OffreStage, Long> {

    List<OffreStage> findByLocalisationContainingIgnoreCaseAndDureeStageAndSecteurNomSecteurContainingIgnoreCase(
            String localisation, Integer dureeStage, String secteurNom);
    List<OffreStage> findByLocalisationContainingIgnoreCase(String localisation);
    List<OffreStage> findByDureeStage(Integer dureeStage);
    List<OffreStage> findBySecteurNomSecteurContainingIgnoreCase(String secteurNom);
    List<OffreStage> findByLocalisationContainingIgnoreCaseAndDureeStage(String localisation, Integer dureeStage);
    List<OffreStage> findByLocalisationContainingIgnoreCaseAndSecteurNomSecteurContainingIgnoreCase(String localisation, String secteurNom);
    List<OffreStage> findByDureeStageAndSecteurNomSecteurContainingIgnoreCase(Integer dureeStage, String secteurNom);
    List<OffreStage> findOffreStageBySecteur(Secteur secteur);
    List<OffreStage> findByEntrepriseId(Long entrepriseId);
    Long countByEntrepriseId(Long entrepriseId);
    
    @Query("SELECT EXTRACT(MONTH FROM o.datePublication) as month, COUNT(o) FROM OffreStage o WHERE EXTRACT(YEAR FROM o.datePublication) = EXTRACT(YEAR FROM CURRENT_DATE) GROUP BY EXTRACT(MONTH FROM o.datePublication) ORDER BY month")
    List<Object[]> countInternshipsByMonth();
    
    @Query("SELECT s.nomSecteur, COUNT(o) FROM OffreStage o JOIN o.secteur s GROUP BY s.nomSecteur ORDER BY COUNT(o) DESC")
    List<Object[]> countInternshipsBySector();
}
