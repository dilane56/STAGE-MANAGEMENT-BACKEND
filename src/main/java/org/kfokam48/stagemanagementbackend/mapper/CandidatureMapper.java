package org.kfokam48.stagemanagementbackend.mapper;

import org.kfokam48.stagemanagementbackend.dto.CandidatureDTO;
import org.kfokam48.stagemanagementbackend.dto.CandidatureResponseDTO;
import org.kfokam48.stagemanagementbackend.dto.OffreStageInCandidatureDTO;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.model.Candidature;
import org.kfokam48.stagemanagementbackend.repository.EtudiantRepository;
import org.kfokam48.stagemanagementbackend.repository.OffreStageRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class CandidatureMapper {
    private final ModelMapper modelMapper;
    private final OffreStageRepository offreStageRepository;
    private final EtudiantRepository etudiantRepository;

    public CandidatureMapper(ModelMapper modelMapper, OffreStageRepository offreStageRepository, EtudiantRepository etudiantRepository) {
        this.modelMapper = modelMapper;
        this.offreStageRepository = offreStageRepository;
        this.etudiantRepository = etudiantRepository;
    }

    public Candidature candidatureDTOToCandidature(CandidatureDTO candidatureDTO){
        Candidature candidature = new Candidature();
        candidature.setDateCandidature(LocalDate.now());
        candidature.setEtudiant(etudiantRepository.findById(candidatureDTO.getEtudiantId())
                .orElseThrow(() -> new RessourceNotFoundException("Etudiant not found")));
        candidature.setStatut(candidatureDTO.getStatutCandidature());
        candidature.setOffreStage(offreStageRepository.findById(candidatureDTO.getOffreStageId())
                .orElseThrow(() -> new RessourceNotFoundException("Offre de stage not found")));
        candidature.setLettreMotivation(candidatureDTO.getLettreMotivation());
        return candidature;
    }
    public CandidatureResponseDTO candidatureToCandidatureResponseDTO(Candidature candidature){
        CandidatureResponseDTO candidatureResponseDTO = new CandidatureResponseDTO();
        candidatureResponseDTO.setId(candidature.getId());
        candidatureResponseDTO.setStatutCandidature(candidature.getStatut());
        candidatureResponseDTO.setLettreMotivation(candidature.getLettreMotivation());
        candidatureResponseDTO.setDateCandidature(candidature.getDateCandidature());
        candidatureResponseDTO.setEtudiantUsername(candidature.getEtudiant().getUsername());
        candidatureResponseDTO.setOffreStage(modelMapper.map(candidature.getOffreStage(), OffreStageInCandidatureDTO.class));
        candidatureResponseDTO.setCheminFichier(candidature.getCvPath());
        return candidatureResponseDTO;

    }

    public List<CandidatureResponseDTO> candidatureToCandidatureResponseDTO(List<Candidature> candidatures){
        return  candidatures.stream()
                .map(this::candidatureToCandidatureResponseDTO)
                .toList();
    }
}
