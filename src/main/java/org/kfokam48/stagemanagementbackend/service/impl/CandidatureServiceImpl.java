package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.candidature.CandidatureDTO;
import org.kfokam48.stagemanagementbackend.dto.candidature.CandidatureResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.enums.StatutCandidature;
import org.kfokam48.stagemanagementbackend.exception.ResourceAlreadyExistException;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.CandidatureMapper;
import org.kfokam48.stagemanagementbackend.minio.MinIOService;
import org.kfokam48.stagemanagementbackend.model.Candidature;
import org.kfokam48.stagemanagementbackend.model.OffreStage;
import org.kfokam48.stagemanagementbackend.repository.CandidatureRepository;
import org.kfokam48.stagemanagementbackend.repository.ConventionStageRepository;
import org.kfokam48.stagemanagementbackend.service.CandidatureService;
import org.kfokam48.stagemanagementbackend.service.auth.CurrentUserService;
import org.kfokam48.stagemanagementbackend.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class CandidatureServiceImpl implements CandidatureService {
    private final CandidatureRepository candidatureRepository;
    private final CandidatureMapper candidatureMapper;
    private final ConventionStageRepository conventionStageRepository;
    private final MinIOService minIOService;
    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;


    public CandidatureServiceImpl(CandidatureRepository candidatureRepository, CandidatureMapper candidatureMapper,
                                  ConventionStageRepository conventionStageRepository, MinIOService minIOService,
                                  NotificationService notificationService, CurrentUserService currentUserService) {
        this.candidatureRepository = candidatureRepository;
        this.candidatureMapper = candidatureMapper;
        this.conventionStageRepository = conventionStageRepository;
        this.minIOService = minIOService;
        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
    }

    @Override
    public CandidatureResponseDTO getCandidatureById(Long id) {
        Candidature candidature = findCandidature(id);
        checkCanRead(candidature);
        return candidatureMapper.candidatureToCandidatureResponseDTO(candidature);
    }

    @Override
    public List<CandidatureResponseDTO> getAllCandidatures() {
        return candidatureMapper.candidatureToCandidatureResponseDTO(candidatureRepository.findAll());
    }

    @Override
    public List<CandidatureResponseDTO> getCandidaturesByEtudiant(Long etudiantId) {
        currentUserService.checkSelfOrAdmin(etudiantId);
        List<Candidature> candidatures = candidatureRepository.findByEtudiantId(etudiantId);
        return candidatureMapper.candidatureToCandidatureResponseDTO(candidatures);
    }

    @Override
    public List<CandidatureResponseDTO> getCandidaturesByEntreprise(Long entrepriseId) {
        currentUserService.checkSelfOrAdmin(entrepriseId);
        List<Candidature> candidatures = candidatureRepository.findByOffreStageEntrepriseId(entrepriseId);
        return candidatureMapper.candidatureToCandidatureResponseDTO(candidatures);
    }

    @Override
    public CandidatureResponseDTO updateCandidature(Long id, Long idEtudiant, Long idOffre, String lettreMotivation, MultipartFile file) throws Exception {
        Candidature candidature = findCandidature(id);
        checkOwnerStudentOrAdmin(candidature);

        if (candidature.getStatut() != StatutCandidature.EN_ATTENTE) {
            throw new IllegalStateException("Seule une candidature en attente peut être modifiée");
        }
        // L'étudiant et l'offre d'une candidature ne sont pas modifiables : on ne réassigne plus ces champs
        if (idOffre != null && !idOffre.equals(candidature.getOffreStage().getId())) {
            throw new IllegalArgumentException("L'offre d'une candidature ne peut pas être modifiée");
        }

        candidature.setLettreMotivation(lettreMotivation);
        candidature.setDateCandidature(LocalDate.now());

        if (file != null && !file.isEmpty()) {
            candidature.setCvPath(minIOService.uploadFile(file));
        }

        candidatureRepository.save(candidature);
        Long entrepriseId = candidature.getOffreStage().getEntreprise().getId();
        notificationService.sendNotification(entrepriseId, "Candidature mise à jour",
                "La candidature de " + candidature.getEtudiant().getFullName() + " pour « "
                        + candidature.getOffreStage().getIntitule() + " » a été mise à jour", false);
        return candidatureMapper.candidatureToCandidatureResponseDTO(candidature);
    }


    @Override
    public CandidatureResponseDTO addCandidature(CandidatureDTO candidatureDTO, MultipartFile file) throws Exception {
        // Un étudiant ne peut postuler qu'en son propre nom
        if (!currentUserService.isAdmin()) {
            currentUserService.checkSelf(candidatureDTO.getEtudiantId());
        }
        if (candidatureDTO.getLettreMotivation() == null || candidatureDTO.getLettreMotivation().isBlank()) {
            throw new IllegalArgumentException("La lettre de motivation est obligatoire");
        }

        boolean exists = candidatureRepository.existsByEtudiantIdAndOffreStageId(
                candidatureDTO.getEtudiantId(), candidatureDTO.getOffreStageId());
        if (exists) {
            throw new ResourceAlreadyExistException("L'étudiant a déjà postulé à cette offre !");
        }

        Candidature candidature = candidatureMapper.candidatureDTOToCandidature(candidatureDTO);

        OffreStage offre = candidature.getOffreStage();
        if (offre.getDateLimiteCandidature() != null && offre.getDateLimiteCandidature().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("La date limite de candidature pour cette offre est dépassée");
        }

        candidature.setDateCandidature(LocalDate.now());
        candidature.setStatut(StatutCandidature.EN_ATTENTE);
        candidature.setLettreMotivation(candidatureDTO.getLettreMotivation());
        // Le CV (PDF vérifié) est stocké dans MinIO, on conserve le nom de l'objet
        candidature.setCvPath(minIOService.uploadFile(file));

        candidatureRepository.save(candidature);
        notificationService.sendNotification(offre.getEntreprise().getId(), "Nouvelle candidature",
                candidature.getEtudiant().getFullName() + " a postulé à votre offre « " + offre.getIntitule() + " »", false);

        return candidatureMapper.candidatureToCandidatureResponseDTO(candidature);
    }

    @Override
    public ResponseEntity<String> updateCandidatureStatut(Long id, String statut, String message) {
        Candidature candidature = findCandidature(id);
        checkOwnerCompanyOrAdmin(candidature);

        StatutCandidature nouveauStatut = parseStatut(statut);
        if (conventionStageRepository.existsByCandidatureId(id)) {
            throw new IllegalStateException("Une convention existe déjà pour cette candidature : son statut ne peut plus changer");
        }

        candidature.setStatut(nouveauStatut);
        candidature.setDateReponse(LocalDate.now());
        candidature.setMessageReponse(message);
        candidatureRepository.save(candidature);

        String libelle = switch (nouveauStatut) {
            case ACCEPTE -> "acceptée";
            case REFUSE -> "refusée";
            case EN_ATTENTE -> "remise en attente";
        };
        notificationService.sendNotification(candidature.getEtudiant().getId(), "Statut de la candidature",
                "Votre candidature pour « " + candidature.getOffreStage().getIntitule() + " » a été " + libelle, false);

        return ResponseEntity.ok("Statut de la candidature mis à jour avec succès");
    }

    @Override
    public ResponseEntity<String> deleteCandidature(Long id) {
        Candidature candidature = findCandidature(id);
        checkOwnerStudentOrAdmin(candidature);

        if (!currentUserService.isAdmin() && candidature.getStatut() != StatutCandidature.EN_ATTENTE) {
            throw new IllegalStateException("Seule une candidature en attente peut être retirée");
        }
        if (conventionStageRepository.existsByCandidatureId(id)) {
            throw new IllegalStateException("Impossible de supprimer une candidature liée à une convention");
        }
        candidatureRepository.delete(candidature);
        return ResponseEntity.ok("Deleted Candidature");
    }

    @Override
    public byte[] downloadCv(Long id) throws Exception {
        Candidature candidature = findCandidature(id);
        checkCanRead(candidature);
        return minIOService.downloadFile(candidature.getCvPath());
    }

    private Candidature findCandidature(Long id) {
        return candidatureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Candidature not found"));
    }

    private static StatutCandidature parseStatut(String statut) {
        if (statut == null) {
            throw new IllegalArgumentException("Le statut est obligatoire");
        }
        try {
            return StatutCandidature.valueOf(statut.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut inconnu : " + statut + " (valeurs possibles : EN_ATTENTE, ACCEPTE, REFUSE)");
        }
    }

    /** Lecture : admin, enseignant, l'étudiant candidat ou l'entreprise qui a publié l'offre. */
    private void checkCanRead(Candidature candidature) {
        if (currentUserService.isAdmin() || currentUserService.hasRole(Roles.ENSEIGNANT)) {
            return;
        }
        Long userId = currentUserService.getCurrentUserId();
        if (!userId.equals(candidature.getEtudiant().getId())
                && !userId.equals(candidature.getOffreStage().getEntreprise().getId())) {
            throw new AccessDeniedException("Vous n'avez pas accès à cette candidature");
        }
    }

    private void checkOwnerStudentOrAdmin(Candidature candidature) {
        currentUserService.checkSelfOrAdmin(candidature.getEtudiant().getId());
    }

    private void checkOwnerCompanyOrAdmin(Candidature candidature) {
        currentUserService.checkSelfOrAdmin(candidature.getOffreStage().getEntreprise().getId());
    }
}
