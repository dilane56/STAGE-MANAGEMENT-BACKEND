package org.kfokam48.stagemanagementbackend.service.impl;

import jakarta.transaction.Transactional;
import org.kfokam48.stagemanagementbackend.dto.CandidatureDTO;
import org.kfokam48.stagemanagementbackend.dto.CandidatureResponseDTO;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.CandidatureMapper;
import org.kfokam48.stagemanagementbackend.model.Candidature;
import org.kfokam48.stagemanagementbackend.repository.CandidatureRepository;
import org.kfokam48.stagemanagementbackend.repository.EtudiantRepository;
import org.kfokam48.stagemanagementbackend.repository.OffreStageRepository;
import org.kfokam48.stagemanagementbackend.service.CandidatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CandidatureServiceImpl implements CandidatureService {
    private final CandidatureRepository candidatureRepository;
    private final CandidatureMapper candidatureMapper;
    private final OffreStageRepository offreStageRepository;
    private final EtudiantRepository etudiantRepository;

    public CandidatureServiceImpl(CandidatureRepository candidatureRepository, CandidatureMapper candidatureMapper, OffreStageRepository offreStageRepository, EtudiantRepository etudiantRepository) {
        this.candidatureRepository = candidatureRepository;
        this.candidatureMapper = candidatureMapper;
        this.offreStageRepository = offreStageRepository;
        this.etudiantRepository = etudiantRepository;
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
        Candidature candidature = candidatureMapper.candidatureDTOToCandidature(candidatureDTO);
        candidature.setDateCandidature(LocalDate.now());
        candidatureRepository.save(candidature);
        return candidatureMapper.candidatureToCandidatureResponseDTO(candidature);
    }

    @Override
    public CandidatureResponseDTO updateCandidature(Long id,CandidatureDTO candidatureDTO) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Candidature not found"));
        candidature.setDateCandidature(LocalDate.now());
        candidature.setStatut(candidatureDTO.getStatutCandidature());
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
}
