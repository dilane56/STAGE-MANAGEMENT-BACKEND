package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.convention.ConventionRequestDTO;
import org.kfokam48.stagemanagementbackend.dto.convention.ConventionResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.enums.StatutCandidature;
import org.kfokam48.stagemanagementbackend.enums.StatutConvention;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.ConventionMapper;
import org.kfokam48.stagemanagementbackend.model.Administrateur;
import org.kfokam48.stagemanagementbackend.model.Candidature;
import org.kfokam48.stagemanagementbackend.model.ConventionStage;
import org.kfokam48.stagemanagementbackend.model.Enseignant;
import org.kfokam48.stagemanagementbackend.repository.AdministrateurRepository;
import org.kfokam48.stagemanagementbackend.repository.CandidatureRepository;
import org.kfokam48.stagemanagementbackend.repository.ConventionStageRepository;
import org.kfokam48.stagemanagementbackend.repository.EnseignantRepository;
import org.kfokam48.stagemanagementbackend.service.ConventionService;
import org.kfokam48.stagemanagementbackend.service.auth.CurrentUserService;
import org.kfokam48.stagemanagementbackend.service.notification.NotificationService;
import org.kfokam48.stagemanagementbackend.service.pdf.PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Cycle de vie d'une convention : EN_ATTENTE (créée par l'entreprise)
 * → VALIDE (enseignant de la filière) → APPROUVE (administrateur).
 */
@Service
@Transactional
public class ConventionServiceImpl implements ConventionService {

    private static final Logger logger = LoggerFactory.getLogger(ConventionServiceImpl.class);

    private final ConventionStageRepository conventionStageRepository;
    private final ConventionMapper conventionMapper;
    private final CandidatureRepository candidatureRepository;
    private final EnseignantRepository enseignantRepository;
    private final AdministrateurRepository administrateurRepository;
    private final NotificationService notificationService;
    private final PdfService pdfService;
    private final CurrentUserService currentUserService;

    public ConventionServiceImpl(ConventionStageRepository conventionStageRepository, ConventionMapper conventionMapper,
                                 CandidatureRepository candidatureRepository, EnseignantRepository enseignantRepository,
                                 AdministrateurRepository administrateurRepository, NotificationService notificationService,
                                 PdfService pdfService, CurrentUserService currentUserService) {
        this.conventionStageRepository = conventionStageRepository;
        this.conventionMapper = conventionMapper;
        this.candidatureRepository = candidatureRepository;
        this.enseignantRepository = enseignantRepository;
        this.administrateurRepository = administrateurRepository;
        this.notificationService = notificationService;
        this.pdfService = pdfService;
        this.currentUserService = currentUserService;
    }

    @Override
    public ConventionResponseDTO findByConventionId(Long conventionId) {
        ConventionStage conventionStage = findConvention(conventionId);
        checkCanRead(conventionStage);
        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }

    @Override
    public ConventionResponseDTO createConvention(ConventionRequestDTO conventionRequestDTO) {
        if (conventionRequestDTO == null || conventionRequestDTO.getIdCandidature() == null) {
            throw new IllegalArgumentException("L'identifiant de la candidature est obligatoire");
        }
        Candidature candidature = candidatureRepository.findById(conventionRequestDTO.getIdCandidature())
                .orElseThrow(() -> new RessourceNotFoundException("Candidature introuvable avec l'ID: " + conventionRequestDTO.getIdCandidature()));

        // Seule l'entreprise qui a publié l'offre (ou un admin) peut créer la convention
        currentUserService.checkSelfOrAdmin(companyIdOf(candidature));

        if (candidature.getStatut() != StatutCandidature.ACCEPTE) {
            throw new IllegalStateException("Une convention ne peut être créée que pour une candidature acceptée. Statut actuel: " + candidature.getStatut());
        }
        if (conventionStageRepository.existsByCandidatureId(candidature.getId())) {
            throw new IllegalStateException("Une convention existe déjà pour cette candidature");
        }

        ConventionStage conventionStage = new ConventionStage();
        conventionStage.setCandidature(candidature);
        conventionStage.setStatutConvention(StatutConvention.EN_ATTENTE);
        conventionStage.setDateCreation(LocalDate.now());
        computeDates(conventionStage);

        conventionStageRepository.save(conventionStage);

        notifyTeachersOfFiliere(conventionStage);
        notificationService.notifyAdmins("Nouvelle convention",
                "Une convention a été créée pour l'étudiant " + candidature.getEtudiant().getFullName());
        notificationService.sendNotification(candidature.getEtudiant().getId(), "Convention de stage créée",
                "Votre convention pour « " + candidature.getOffreStage().getIntitule() + " » est en attente de validation", false);
        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }

    @Override
    public ConventionResponseDTO updateConvention(Long conventionId, Long idCandidature) {
        ConventionStage conventionStage = findConvention(conventionId);
        currentUserService.checkSelfOrAdmin(companyIdOf(conventionStage.getCandidature()));
        checkNotLocked(conventionStage, "modifier");

        if (idCandidature != null && !idCandidature.equals(conventionStage.getCandidature().getId())) {
            Candidature candidature = candidatureRepository.findById(idCandidature)
                    .orElseThrow(() -> new RessourceNotFoundException("Candidature introuvable avec l'ID: " + idCandidature));
            currentUserService.checkSelfOrAdmin(companyIdOf(candidature));
            if (candidature.getStatut() != StatutCandidature.ACCEPTE) {
                throw new IllegalStateException("La candidature doit être acceptée");
            }
            if (conventionStageRepository.existsByCandidatureId(idCandidature)) {
                throw new IllegalStateException("Une convention existe déjà pour cette candidature");
            }
            conventionStage.setCandidature(candidature);
        }

        computeDates(conventionStage);
        conventionStage.setStatutConvention(StatutConvention.EN_ATTENTE);
        conventionStageRepository.save(conventionStage);

        notifyTeachersOfFiliere(conventionStage);
        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }

    @Override
    public String deleteConvention(Long conventionId) {
        ConventionStage conventionStage = findConvention(conventionId);
        currentUserService.checkSelfOrAdmin(companyIdOf(conventionStage.getCandidature()));
        checkNotLocked(conventionStage, "supprimer");

        conventionStageRepository.delete(conventionStage);
        return "Convention supprimée avec succès";
    }

    @Override
    public List<ConventionResponseDTO> findAllConventions() {
        if (!currentUserService.isAdmin()) {
            throw new AccessDeniedException("Réservé aux administrateurs");
        }
        return conventionMapper.conventionStageToConventionResponseDTOList(conventionStageRepository.findAll());
    }

    @Override
    public List<ConventionResponseDTO> findConventionsByEntreprise(Long entrepriseId) {
        currentUserService.checkSelfOrAdmin(entrepriseId);
        List<ConventionStage> conventions = conventionStageRepository.findByCandidatureOffreStageEntrepriseId(entrepriseId);
        return conventionMapper.conventionStageToConventionResponseDTOList(conventions);
    }

    @Override
    public List<ConventionResponseDTO> findConventionsByEnseignant(Long enseignantId) {
        currentUserService.checkSelfOrAdmin(enseignantId);
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RessourceNotFoundException("Enseignant introuvable avec l'ID: " + enseignantId));

        List<ConventionStage> conventions = conventionStageRepository.findByCandidatureEtudiantFiliere(enseignant.getFiliere());
        return conventionMapper.conventionStageToConventionResponseDTOList(conventions);
    }

    @Override
    public ConventionResponseDTO validateConventionByEnseignant(Long enseigantId, Long conventionId) {
        // Un enseignant ne peut valider qu'en son propre nom
        currentUserService.checkSelf(enseigantId);
        Enseignant enseignant = enseignantRepository.findById(enseigantId).orElseThrow(()-> new RessourceNotFoundException("Enseignant not Found"));
        ConventionStage conventionStage = findConvention(conventionId);

        if (conventionStage.getStatutConvention() != StatutConvention.EN_ATTENTE) {
            throw new IllegalStateException("Seule une convention en attente peut être validée. Statut actuel: " + conventionStage.getStatutConvention());
        }
        String filiereEtudiant = conventionStage.getCandidature().getEtudiant().getFiliere();
        if (!Objects.equals(enseignant.getFiliere(), filiereEtudiant)) {
            throw new AccessDeniedException("Cette convention ne concerne pas un étudiant de votre filière");
        }

        conventionStage.setDateValidation(LocalDate.now());
        conventionStage.setStatutConvention(StatutConvention.VALIDE);
        conventionStage.setEnseignantValideur(enseignant);
        conventionStageRepository.save(conventionStage);

        notificationService.sendNotification(companyIdOf(conventionStage.getCandidature()), "Convention validée",
                "Votre convention du " + conventionStage.getDateCreation() + " a été validée par l'enseignant", false);
        notificationService.notifyAdmins("Convention à approuver",
                "La convention N° " + conventionStage.getId() + " a été validée, votre approbation est attendue");
        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }

    @Override
    public ConventionResponseDTO approuveConventionByAdministrator(Long administratorId, Long conventionId) {
        currentUserService.checkSelf(administratorId);
        Administrateur administrateur = administrateurRepository.findById(administratorId).orElseThrow(()-> new RessourceNotFoundException("Administrateur not Found"));
        ConventionStage conventionStage = findConvention(conventionId);

        if (conventionStage.getStatutConvention() != StatutConvention.VALIDE) {
            throw new IllegalStateException("La convention doit d'abord être validée par un enseignant. Statut actuel: " + conventionStage.getStatutConvention());
        }

        conventionStage.setDateAprouval(LocalDate.now());
        conventionStage.setStatutConvention(StatutConvention.APPROUVE);
        conventionStage.setAprouvalAdministrator(administrateur);
        conventionStageRepository.save(conventionStage);

        notificationService.sendNotification(companyIdOf(conventionStage.getCandidature()), "Convention approuvée",
                "Votre convention du " + conventionStage.getDateCreation() + " a été approuvée", false);
        notificationService.sendNotification(conventionStage.getCandidature().getEtudiant().getId(), "Convention approuvée",
                "Votre convention de stage est désormais disponible", false);
        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }

    @Override
    public ResponseEntity<byte[]> generateConventionPdf(Long conventionId) throws Exception {
        ConventionStage conventionStage = findConvention(conventionId);
        checkCanRead(conventionStage);

        ByteArrayOutputStream pdfStream = pdfService.generateConventionPdf(conventionStage);
        byte[] pdfBytes = pdfStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("convention_" + conventionId + ".pdf").build());

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private ConventionStage findConvention(Long conventionId) {
        return conventionStageRepository.findById(conventionId)
                .orElseThrow(() -> new RessourceNotFoundException("Convention introuvable avec l'ID: " + conventionId));
    }

    private static Long companyIdOf(Candidature candidature) {
        return candidature.getOffreStage().getEntreprise().getId();
    }

    private static void checkNotLocked(ConventionStage conventionStage, String action) {
        if (conventionStage.getStatutConvention() == StatutConvention.VALIDE ||
                conventionStage.getStatutConvention() == StatutConvention.APPROUVE) {
            throw new IllegalStateException("Impossible de " + action + " une convention validée ou approuvée. Statut actuel: " + conventionStage.getStatutConvention());
        }
    }

    /**
     * Le stage commence à la date de début prévue dans l'offre (auparavant : date du jour de création),
     * et dure le nombre de mois de l'offre.
     */
    private static void computeDates(ConventionStage conventionStage) {
        var offre = conventionStage.getCandidature().getOffreStage();
        LocalDate debut = offre.getDateDebutStage() != null ? offre.getDateDebutStage() : LocalDate.now();
        conventionStage.setDateDebut(debut);
        conventionStage.setDateFin(debut.plusMonths(offre.getDureeStage()));
    }

    /** Lecture : admin, enseignant, entreprise propriétaire ou étudiant concerné. */
    private void checkCanRead(ConventionStage conventionStage) {
        if (currentUserService.isAdmin() || currentUserService.hasRole(Roles.ENSEIGNANT)) {
            return;
        }
        Long userId = currentUserService.getCurrentUserId();
        Candidature candidature = conventionStage.getCandidature();
        if (!userId.equals(companyIdOf(candidature)) && !userId.equals(candidature.getEtudiant().getId())) {
            throw new AccessDeniedException("Vous n'avez pas accès à cette convention");
        }
    }

    private void notifyTeachersOfFiliere(ConventionStage conventionStage) {
        String filiereEtudiant = conventionStage.getCandidature().getEtudiant().getFiliere();
        for (Enseignant enseignant : enseignantRepository.findByFiliere(filiereEtudiant)) {
            try {
                notificationService.sendNotification(
                        enseignant.getId(),
                        "Nouvelle convention à valider",
                        "Une convention de stage a été créée pour l'étudiant " +
                                conventionStage.getCandidature().getEtudiant().getFullName() +
                                " (" + filiereEtudiant + "). Veuillez la valider.",
                        false
                );
            } catch (RuntimeException e) {
                // Une notification en échec ne doit pas annuler la convention
                logger.warn("Notification à l'enseignant {} impossible : {}", enseignant.getId(), e.getMessage());
            }
        }
    }
}
