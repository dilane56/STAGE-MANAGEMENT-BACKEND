package org.kfokam48.stagemanagementbackend.service.impl;

import jakarta.transaction.Transactional;
import org.kfokam48.stagemanagementbackend.dto.CandidatureDTO;
import org.kfokam48.stagemanagementbackend.dto.CandidatureResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.StatutCandidature;
import org.kfokam48.stagemanagementbackend.exception.ResourceAlreadyExistException;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.CandidatureMapper;
import org.kfokam48.stagemanagementbackend.minio.MinIOService;
import org.kfokam48.stagemanagementbackend.model.Candidature;
import org.kfokam48.stagemanagementbackend.notification.CandidatureNotificationHandler;
import org.kfokam48.stagemanagementbackend.repository.CandidatureRepository;
import org.kfokam48.stagemanagementbackend.repository.EtudiantRepository;
import org.kfokam48.stagemanagementbackend.repository.OffreStageRepository;
import org.kfokam48.stagemanagementbackend.service.CandidatureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CandidatureServiceImpl implements CandidatureService {
    private final CandidatureRepository candidatureRepository;
    private final CandidatureMapper candidatureMapper;
    private final OffreStageRepository offreStageRepository;
    private final EtudiantRepository etudiantRepository;
    private final CandidatureNotificationHandler notificationHandler;
    private final MinIOService minIOService;


    public CandidatureServiceImpl(CandidatureRepository candidatureRepository, CandidatureMapper candidatureMapper, OffreStageRepository offreStageRepository, EtudiantRepository etudiantRepository, CandidatureNotificationHandler notificationHandler, MinIOService minIOService) {
        this.candidatureRepository = candidatureRepository;
        this.candidatureMapper = candidatureMapper;
        this.offreStageRepository = offreStageRepository;
        this.etudiantRepository = etudiantRepository;
        this.notificationHandler = notificationHandler;
        this.minIOService = minIOService;
    }

    @Override
    public CandidatureResponseDTO getCandidatureById(Long id) {
       Candidature candidature = candidatureRepository.findById(id)
               .orElseThrow(() -> new RessourceNotFoundException("Candidature not found"));
       return candidatureMapper.candidatureToCandidatureResponseDTO(candidature);
    }

    @Override
    public List<CandidatureResponseDTO> getAllCandidatures() {
        return candidatureMapper.candidatureToCandidatureResponseDTO(candidatureRepository.findAll());
    }

    @Override
    public CandidatureResponseDTO addCandidature(CandidatureDTO candidatureDTO) {
        // ✅ Vérification en base de données
        boolean exists = candidatureRepository.existsByEtudiantIdAndOffreStageId(
                candidatureDTO.getEtudiantId(), candidatureDTO.getOffreStageId());

        if (exists) {
            throw new ResourceAlreadyExistException("L'étudiant a déjà postulé à cette offre !");
        }
        Candidature candidature = candidatureMapper.candidatureDTOToCandidature(candidatureDTO);
       candidature.setDateCandidature(LocalDate.now());
        candidatureRepository.save(candidature);
        return candidatureMapper.candidatureToCandidatureResponseDTO(candidature);

    }

    @Override
    public CandidatureResponseDTO addCandidature(CandidatureDTO candidatureDTO, MultipartFile file) throws Exception {
        // ✅ Vérification en base de données
        boolean exists = candidatureRepository.existsByEtudiantIdAndOffreStageId(
                candidatureDTO.getEtudiantId(), candidatureDTO.getOffreStageId());

        if (exists) {
            throw new ResourceAlreadyExistException("L'étudiant a déjà postulé à cette offre !");
        }


        // ✅ Mapper l'objet DTO en entité
        Candidature candidature = candidatureMapper.candidatureDTOToCandidature(candidatureDTO);

        candidature.setDateCandidature(LocalDate.now());
        candidature.setStatut(StatutCandidature.EN_ATTENTE);

        // ✅ 1. Uploader le fichier CV dans MinIO
        String fileUrl = minIOService.uploadFile(file); // 🚀 Envoi du fichier

        // ✅ 2. Ajouter l’URL du CV à la candidature
        candidature.setCvPath(fileUrl);

        // ✅ 3. Enregistrer la candidature avec l’URL du CV
        candidatureRepository.save(candidature);

        return candidatureMapper.candidatureToCandidatureResponseDTO(candidature);
    }

    @Override
    public ResponseEntity<String> updateCandidatureStatut(Long id, String statut) throws IOException {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Candidature not found"));

        candidature.setStatut(StatutCandidature.valueOf(statut)); // Conversion string → enum
        candidatureRepository.save(candidature);

        // Envoi d'une notification en temps réel via WebSocket
        String message = "Votre candidature pour " + candidature.getOffreStage().getIntitule() +
                " a changé de statut : " + candidature.getStatut();
        notificationHandler.sendNotification(candidature.getEtudiant().getId(), message);

        return ResponseEntity.ok("Statut de la candidature mis à jour avec succès");
    }






    @Override
    public CandidatureResponseDTO updateCandidature(Long id,CandidatureDTO candidatureDTO) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Candidature not found"));
        candidature.setDateCandidature(LocalDate.now());
        candidature.setLettreMotivation(candidature.getLettreMotivation());
        candidature.setOffreStage(offreStageRepository.findById(candidatureDTO.getOffreStageId()).orElseThrow(() -> new RessourceNotFoundException("Offre stage not found")));
        candidature.setEtudiant(etudiantRepository.findById(candidatureDTO.getEtudiantId()).orElseThrow(() -> new RessourceNotFoundException("Etudiant not found")));
        candidatureRepository.save(candidature);


        return candidatureMapper.candidatureToCandidatureResponseDTO(candidature);
    }

    @Override
    public ResponseEntity<String> deleteCandidature(Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Candidature not found"));
        candidatureRepository.deleteById(id);
        return ResponseEntity.ok("Deleted Candidature");
    }

    @Override
    public  ResponseEntity<String> uploadImage(MultipartFile file, Long id) throws Exception {
        Candidature candidature = candidatureRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Candidature not found"));
        // Définir le chemin du fichier

        String fileUrl = minIOService.uploadFile(file);
        candidature.setCvPath(fileUrl);
        candidature.setCvPath(fileUrl);
        candidatureRepository.save(candidature);
        return ResponseEntity.ok("CV uploaded");

    }
}
