package org.kfokam48.stagemanagementbackend.mapper;

import org.kfokam48.stagemanagementbackend.dto.offreStage.CandidatureInOffreStageDTO;
import org.kfokam48.stagemanagementbackend.dto.offreStage.OffreStageDTO;
import org.kfokam48.stagemanagementbackend.dto.offreStage.OffreStageResponseDTO;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.model.Candidature;
import org.kfokam48.stagemanagementbackend.model.OffreStage;
import org.kfokam48.stagemanagementbackend.repository.EntrepriseRepository;
import org.kfokam48.stagemanagementbackend.repository.SecteurRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OffreStageMapper {
    private final EntrepriseRepository entrepriseRepository;
    private final SecteurRepository secteurRepository;

    public OffreStageMapper(EntrepriseRepository entrepriseRepository, SecteurRepository secteurRepository) {
        this.entrepriseRepository = entrepriseRepository;
        this.secteurRepository = secteurRepository;
    }

    public OffreStage offreStageDTOToOffreStage(OffreStageDTO offreStageDTO) {
        if (offreStageDTO.getSecteurId() == null) {
            throw new IllegalArgumentException("Le secteur de l'offre est obligatoire");
        }
        if (offreStageDTO.getEntrepriseId() == null) {
            throw new IllegalArgumentException("L'entreprise de l'offre est obligatoire");
        }
        OffreStage offreStage = new OffreStage();
        offreStage.setDescription(offreStageDTO.getDescription());
        offreStage.setDureeStage(offreStageDTO.getDuree());
        offreStage.setSecteur(secteurRepository.findById(offreStageDTO.getSecteurId()).orElseThrow(()->new RessourceNotFoundException("Secteur not found")));
        offreStage.setCompetences(offreStageDTO.getCompetences() != null ? new ArrayList<>(offreStageDTO.getCompetences()) : new ArrayList<>());
        offreStage.setEntreprise(entrepriseRepository.findById(offreStageDTO.getEntrepriseId())
                .orElseThrow(() -> new RessourceNotFoundException("Entreprise not found")));
        offreStage.setIntitule(offreStageDTO.getIntitule());
        offreStage.setLocalisation(offreStageDTO.getLocalisation());
        offreStage.setDateDebutStage(offreStageDTO.getDateDebutStage());
        offreStage.setNombrePlaces(offreStageDTO.getNombrePlaces());
        offreStage.setDateLimiteCandidature(offreStageDTO.getDateLimiteCandidature());
        return offreStage;
    }

    /**
     * @param includeCandidatures true seulement pour l'entreprise propriétaire ou un administrateur
     *                            (auparavant la liste des candidats était visible par tous les étudiants).
     */
    public OffreStageResponseDTO offreStageToOffreStageResponseDTO(OffreStage offreStage, boolean includeCandidatures) {
        OffreStageResponseDTO offreStageResponseDTO = new OffreStageResponseDTO();
        offreStageResponseDTO.setId(offreStage.getId());
        offreStageResponseDTO.setIntitule(offreStage.getIntitule());
        offreStageResponseDTO.setDescription(offreStage.getDescription());
        if (offreStage.getSecteur() != null) {
            offreStageResponseDTO.setSecteurName(offreStage.getSecteur().getNomSecteur());
            offreStageResponseDTO.setSecteurId(offreStage.getSecteur().getId());
        }
        offreStageResponseDTO.setCompetences(offreStage.getCompetences());
        offreStageResponseDTO.setLocalisation(offreStage.getLocalisation());
        offreStageResponseDTO.setDureeStage(offreStage.getDureeStage());
        offreStageResponseDTO.setNomEntreprise(offreStage.getEntreprise().getFullName());
        offreStageResponseDTO.setEntrepriseId(offreStage.getEntreprise().getId());
        offreStageResponseDTO.setDatePublication(offreStage.getDatePublication());
        offreStageResponseDTO.setDateDebutStage(offreStage.getDateDebutStage());
        offreStageResponseDTO.setDateLimiteCandidature(offreStage.getDateLimiteCandidature());
        offreStageResponseDTO.setNombrePlaces(offreStage.getNombrePlaces());
        offreStageResponseDTO.setCandidatures(includeCandidatures
                ? offreStage.getCandidatures().stream().map(this::toCandidatureInOffre).toList()
                : List.of());
        return offreStageResponseDTO;
    }

    private CandidatureInOffreStageDTO toCandidatureInOffre(Candidature candidature) {
        CandidatureInOffreStageDTO dto = new CandidatureInOffreStageDTO();
        dto.setIdCandidature(candidature.getId());
        dto.setEtudiantUsername(candidature.getEtudiant() != null ? candidature.getEtudiant().getFullName() : null);
        dto.setDateCandidature(candidature.getDateCandidature());
        dto.setStatutCandidature(candidature.getStatut());
        return dto;
    }
}
