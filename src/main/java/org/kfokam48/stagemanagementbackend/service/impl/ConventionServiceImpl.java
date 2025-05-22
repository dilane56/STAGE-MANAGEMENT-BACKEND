package org.kfokam48.stagemanagementbackend.service.impl;

import jakarta.transaction.Transactional;
import org.kfokam48.stagemanagementbackend.dto.convention.ConventionRequestDTO;
import org.kfokam48.stagemanagementbackend.dto.convention.ConventionResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.StatutConvention;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.CandidatureMapper;
import org.kfokam48.stagemanagementbackend.mapper.ConventionMapper;
import org.kfokam48.stagemanagementbackend.minio.MinIOService;
import org.kfokam48.stagemanagementbackend.model.Administrateur;
import org.kfokam48.stagemanagementbackend.model.Candidature;
import org.kfokam48.stagemanagementbackend.model.ConventionStage;
import org.kfokam48.stagemanagementbackend.model.Enseignant;
import org.kfokam48.stagemanagementbackend.notification.CandidatureNotificationHandler;
import org.kfokam48.stagemanagementbackend.repository.AdministrateurRepository;
import org.kfokam48.stagemanagementbackend.repository.CandidatureRepository;
import org.kfokam48.stagemanagementbackend.repository.ConventionStageRepository;
import org.kfokam48.stagemanagementbackend.repository.EnseignantRepository;
import org.kfokam48.stagemanagementbackend.service.ConventionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
@Service
@Transactional
public class ConventionServiceImpl implements ConventionService {
    private final ConventionStageRepository conventionStageRepository;
    private final ConventionMapper conventionMapper;
    private final MinIOService minIOService;
    private final CandidatureMapper candidatureMapper;
    private final CandidatureRepository candidatureRepository;
    private final EnseignantRepository enseignantRepository;
    private final CandidatureNotificationHandler notificationHandler;
    private final AdministrateurRepository administrateurRepository;

    public ConventionServiceImpl(ConventionStageRepository conventionStageRepository, ConventionMapper conventionMapper, MinIOService minIOService, CandidatureMapper candidatureMapper, CandidatureRepository candidatureRepository, EnseignantRepository enseignantRepository, CandidatureNotificationHandler notificationHandler, AdministrateurRepository administrateurRepository) {
        this.conventionStageRepository = conventionStageRepository;
        this.conventionMapper = conventionMapper;
        this.minIOService = minIOService;
        this.candidatureMapper = candidatureMapper;
        this.candidatureRepository = candidatureRepository;
        this.enseignantRepository = enseignantRepository;
        this.notificationHandler = notificationHandler;
        this.administrateurRepository = administrateurRepository;
    }

    @Override
    public ConventionResponseDTO findByConventionId(Long conventionId) {
        ConventionStage conventionStage = conventionStageRepository.findById(conventionId).orElseThrow(()-> new RessourceNotFoundException("Convention not Found"));
        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }

    @Override
    public ConventionResponseDTO createConvention(ConventionRequestDTO conventionRequestDTO, MultipartFile file) throws Exception {
        ConventionStage conventionStage = conventionMapper.conventionRequestDTOToCoventionStage(conventionRequestDTO);
        conventionStage.setStatutConvention(StatutConvention.EN_ATTENTE);
        conventionStage.setDateCreation(LocalDate.now());
        // ✅ Upload du fichier vers MinIO
        String fileUrl = minIOService.uploadFile(file);
        conventionStage.setPdfConventionPath(fileUrl);
        conventionStageRepository.save(conventionStage);
        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }
    @Override
    public ConventionResponseDTO updateConvention(ConventionRequestDTO conventionRequestDTO, Long conventionId, MultipartFile file) throws Exception {
        ConventionStage conventionStage = conventionStageRepository.findById(conventionId).orElseThrow(()-> new RessourceNotFoundException("Convention not Found"));
        Candidature candidature = candidatureRepository.findById(conventionRequestDTO.getIdCandidature()).orElseThrow(()-> new RessourceNotFoundException("Candidature not Found"));
        conventionStage.setCandidature(candidature);
        conventionStage.setStatutConvention(StatutConvention.EN_ATTENTE);
        conventionStage.setDateCreation(LocalDate.now());
        // Ajout du fichier dans MinIO
        String fileUrl = minIOService.uploadFile(file);
        conventionStage.setPdfConventionPath(fileUrl);
        conventionStageRepository.save(conventionStage);
        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }

    @Override
    public String deleteConvention(Long conventionId) {
        ConventionStage conventionStage = conventionStageRepository.findById(conventionId).orElseThrow(()-> new RessourceNotFoundException("Convention not Found"));
        return "Convention Deleted Successfully";
    }

    @Override
    public List<ConventionResponseDTO> findAllConventions() {
        return conventionMapper.conventionStageToConventionResponseDTOList(conventionStageRepository.findAll());
    }

    @Override
    public ConventionResponseDTO validateConventionByEnseignant(Long enseigantId, Long conventionId) throws IOException {
        Enseignant enseignant = enseignantRepository.findById(enseigantId).orElseThrow(()-> new RessourceNotFoundException("Enseignant not Found"));
        ConventionStage conventionStage = conventionStageRepository.findById(conventionId).orElseThrow(()-> new RessourceNotFoundException("Convention not Found"));
        conventionStage.setDateValidation(LocalDate.now());
        conventionStage.setStatutConvention(StatutConvention.VALIDE);
        conventionStage.setEnseignantValideur(enseignant);
        conventionStageRepository.save(conventionStage);
        Long entrepriseID = conventionStage.getCandidature().getOffreStage().getEntreprise().getId();
        String message = "Votre convention du : "+conventionStage.getDateCreation()+" a été validé ";
        notificationHandler.sendNotification(entrepriseID,message);

        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }

    @Override
    public ConventionResponseDTO approuveConventionByAdministrator(Long administratorId, Long conventionId) throws IOException {
        Administrateur administrateur = administrateurRepository.findById(administratorId).orElseThrow(()-> new RessourceNotFoundException("Administrateur not Found"));
        ConventionStage conventionStage = conventionStageRepository.findById(conventionId).orElseThrow(()-> new RessourceNotFoundException("Convention not Found"));
        conventionStage.setDateAprouval(LocalDate.now());
        conventionStage.setStatutConvention(StatutConvention.APPROUVE);
        conventionStage.setAprouvalAdministrator(administrateur);
        conventionStageRepository.save(conventionStage);
        Long entrepriseID = conventionStage.getCandidature().getOffreStage().getEntreprise().getId();
        String message =  "Votre convention du : "+conventionStage.getDateCreation()+" a été Approuvé ";
        notificationHandler.sendNotification(entrepriseID,message);

        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }
    @Override
    public ResponseEntity<byte[]> downloadConvention(Long conventionId) throws Exception {
        ConventionStage conventionStage = conventionStageRepository.findById(conventionId)
                .orElseThrow(() -> new RessourceNotFoundException("Convention not Found"));

        String fileUrl = conventionStage.getPdfConventionPath();

        // ✅ Télécharger le fichier depuis MinIO via URL
        InputStream inputStream = new URL(fileUrl).openStream();
        byte[] fileBytes = inputStream.readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok().headers(headers).body(fileBytes);
    }

}
