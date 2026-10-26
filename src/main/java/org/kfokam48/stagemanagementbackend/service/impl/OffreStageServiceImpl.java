package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.offreStage.OffreStageDTO;
import org.kfokam48.stagemanagementbackend.dto.offreStage.OffreStageResponseDTO;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.OffreStageMapper;
import org.kfokam48.stagemanagementbackend.model.OffreStage;
import org.kfokam48.stagemanagementbackend.repository.OffreStageRepository;
import org.kfokam48.stagemanagementbackend.repository.SecteurRepository;
import org.kfokam48.stagemanagementbackend.service.OffreStageService;
import org.kfokam48.stagemanagementbackend.service.auth.CurrentUserService;
import org.kfokam48.stagemanagementbackend.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.kfokam48.stagemanagementbackend.service.impl.UserAccountHelper.keepIfNull;

@Service
@Transactional
public class OffreStageServiceImpl implements OffreStageService {
    private final OffreStageRepository offreStageRepository;
    private final OffreStageMapper offreStageMapper;
    private final SecteurRepository secteurRepository;
    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    public OffreStageServiceImpl(OffreStageRepository offreStageRepository, OffreStageMapper offreStageMapper,
                                 SecteurRepository secteurRepository, NotificationService notificationService,
                                 CurrentUserService currentUserService) {
        this.offreStageRepository = offreStageRepository;
        this.offreStageMapper = offreStageMapper;
        this.secteurRepository = secteurRepository;
        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public OffreStageResponseDTO getOffreStageById(Long id) {
        return toResponse(findOffre(id));
    }

    @Override
    public ResponseEntity<String> deleteOffreStageById(Long id) {
        OffreStage offreStage = findOffre(id);
        if (!offreStage.getCandidatures().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer une offre ayant déjà reçu des candidatures");
        }
        offreStageRepository.delete(offreStage);
        return ResponseEntity.ok("Offre de stage deleted successfully");
    }

    @Override
    public OffreStageResponseDTO createOffreStage(OffreStageDTO offreStageDTO) {
        // Une entreprise ne peut publier qu'en son nom
        if (!currentUserService.isAdmin()) {
            offreStageDTO.setEntrepriseId(currentUserService.getCurrentUserId());
        }
        validate(offreStageDTO);
        OffreStage offreStage = offreStageMapper.offreStageDTOToOffreStage(offreStageDTO);
        offreStage.setDatePublication(LocalDate.now());
        offreStageRepository.save(offreStage);
        notificationService.notifyAdmins("Nouvelle offre de stage",
                offreStage.getEntreprise().getFullName() + " a publié l'offre « " + offreStage.getIntitule() + " »");
        return toResponse(offreStage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OffreStageResponseDTO> filterOffresStage(String localisation, Integer duree, String secteurNom) {
        List<OffreStage> offres;

        if (localisation != null && duree != null && secteurNom != null) {
            offres = offreStageRepository.findByLocalisationContainingIgnoreCaseAndDureeStageAndSecteurNomSecteurContainingIgnoreCase(localisation, duree, secteurNom);
        } else if (localisation != null && duree != null) {
            offres = offreStageRepository.findByLocalisationContainingIgnoreCaseAndDureeStage(localisation, duree);
        } else if (localisation != null && secteurNom != null) {
            offres = offreStageRepository.findByLocalisationContainingIgnoreCaseAndSecteurNomSecteurContainingIgnoreCase(localisation, secteurNom);
        } else if (duree != null && secteurNom != null) {
            offres = offreStageRepository.findByDureeStageAndSecteurNomSecteurContainingIgnoreCase(duree, secteurNom);
        } else if (localisation != null) {
            offres = offreStageRepository.findByLocalisationContainingIgnoreCase(localisation);
        } else if (duree != null) {
            offres = offreStageRepository.findByDureeStage(duree);
        } else if (secteurNom != null) {
            offres = offreStageRepository.findBySecteurNomSecteurContainingIgnoreCase(secteurNom);
        } else {
            offres = offreStageRepository.findAll();
        }

        return toResponses(offres);
    }


    @Override
    public OffreStageResponseDTO updateOffreStage(Long id, OffreStageDTO offreStageDTO) {
        OffreStage offreStage = findOffre(id);
        checkOwnerOrAdmin(offreStage);

        if (offreStageDTO.getIntitule() != null && offreStageDTO.getIntitule().isBlank()) {
            throw new IllegalArgumentException("L'intitulé de l'offre ne peut pas être vide");
        }
        if (offreStageDTO.getDuree() < 0 || offreStageDTO.getNombrePlaces() < 0) {
            throw new IllegalArgumentException("La durée et le nombre de places doivent être positifs");
        }

        offreStage.setIntitule(keepIfNull(offreStageDTO.getIntitule(), offreStage.getIntitule()));
        offreStage.setDescription(keepIfNull(offreStageDTO.getDescription(), offreStage.getDescription()));
        // Le secteur n'était pas renvoyé au frontend : un secteur absent conserve désormais la valeur existante
        if (offreStageDTO.getSecteurId() != null) {
            offreStage.setSecteur(secteurRepository.findById(offreStageDTO.getSecteurId())
                    .orElseThrow(() -> new RessourceNotFoundException("Secteur not Found")));
        }
        if (offreStageDTO.getCompetences() != null) {
            offreStage.getCompetences().clear();
            offreStage.getCompetences().addAll(offreStageDTO.getCompetences());
        }
        offreStage.setLocalisation(keepIfNull(offreStageDTO.getLocalisation(), offreStage.getLocalisation()));
        if (offreStageDTO.getDuree() > 0) {
            offreStage.setDureeStage(offreStageDTO.getDuree());
        }
        if (offreStageDTO.getNombrePlaces() > 0) {
            offreStage.setNombrePlaces(offreStageDTO.getNombrePlaces());
        }
        offreStage.setDateDebutStage(keepIfNull(offreStageDTO.getDateDebutStage(), offreStage.getDateDebutStage()));
        offreStage.setDateLimiteCandidature(keepIfNull(offreStageDTO.getDateLimiteCandidature(), offreStage.getDateLimiteCandidature()));
        // La date de publication et l'entreprise propriétaire ne changent plus lors d'une modification
        offreStageRepository.save(offreStage);
        return toResponse(offreStage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OffreStageResponseDTO> getAllOffresStage() {
        return toResponses(offreStageRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OffreStageResponseDTO> getOffresByEntreprise(Long entrepriseId) {
        currentUserService.checkSelfOrAdmin(entrepriseId);
        return toResponses(offreStageRepository.findByEntrepriseId(entrepriseId));
    }

    @Override
    public OffreStageResponseDTO addCompetence(String competence, Long id) {
        OffreStage offreStage = findOffre(id);
        checkOwnerOrAdmin(offreStage);
        if (competence == null || competence.isBlank()) {
            throw new IllegalArgumentException("La compétence ne peut pas être vide");
        }
        offreStage.getCompetences().add(competence.trim());
        offreStageRepository.save(offreStage);
        return toResponse(offreStage);
    }

    private OffreStage findOffre(Long id) {
        return offreStageRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Offre de stage not found"));
    }

    private void checkOwnerOrAdmin(OffreStage offreStage) {
        currentUserService.checkSelfOrAdmin(offreStage.getEntreprise().getId());
    }

    private void validate(OffreStageDTO dto) {
        if (dto.getIntitule() == null || dto.getIntitule().isBlank()) {
            throw new IllegalArgumentException("L'intitulé de l'offre est obligatoire");
        }
        if (dto.getDuree() <= 0) {
            throw new IllegalArgumentException("La durée du stage doit être supérieure à 0");
        }
        if (dto.getNombrePlaces() <= 0) {
            throw new IllegalArgumentException("Le nombre de places doit être supérieur à 0");
        }
    }

    private OffreStageResponseDTO toResponse(OffreStage offre) {
        boolean canSeeCandidatures = currentUserService.isAdmin()
                || currentUserService.getCurrentUserId().equals(offre.getEntreprise().getId());
        return offreStageMapper.offreStageToOffreStageResponseDTO(offre, canSeeCandidatures);
    }

    private List<OffreStageResponseDTO> toResponses(List<OffreStage> offres) {
        return offres.stream().map(this::toResponse).toList();
    }
}
