package org.kfokam48.stagemanagementbackend.mapper;

import org.kfokam48.stagemanagementbackend.dto.CandidatureInOffreStageDTO;
import org.kfokam48.stagemanagementbackend.dto.OffreStageDTO;
import org.kfokam48.stagemanagementbackend.dto.OffreStageResponseDTO;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.model.OffreStage;
import org.kfokam48.stagemanagementbackend.repository.EntrepriseRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OffreStageMapper {
    private final ModelMapper modelMapper;
    private final EntrepriseRepository entrepriseRepository;

    public OffreStageMapper(ModelMapper modelMapper, EntrepriseRepository entrepriseRepository) {
        this.modelMapper = modelMapper;
        this.entrepriseRepository = entrepriseRepository;
    }

    public OffreStage offreStageDTOToOffreStage(OffreStageDTO offreStageDTO) {
       OffreStage offreStage = new OffreStage();
        offreStage.setDescription(offreStageDTO.getDescription());
        offreStage.setDuree(offreStageDTO.getDuree());
       offreStage.setDomaine(offreStageDTO.getDomaine());
       offreStage.setEntreprise(entrepriseRepository.findById(offreStageDTO.getEntrepriseId())
                .orElseThrow(() -> new RessourceNotFoundException("Entreprise not found")));
       offreStage.setIntitule(offreStageDTO.getIntitule());
       offreStage.setLocalisation(offreStageDTO.getLocalisation());
        return offreStage;
    }

    public OffreStageResponseDTO offreStageToOffreStageResponseDTO(OffreStage offreStage) {
        OffreStageResponseDTO offreStageResponseDTO = new OffreStageResponseDTO();
        offreStageResponseDTO.setId(offreStage.getId());
        offreStageResponseDTO.setIntitule(offreStage.getIntitule());
        offreStageResponseDTO.setDescription(offreStage.getDescription());
        offreStageResponseDTO.setDomaine(offreStage.getDomaine());
        offreStageResponseDTO.setLocalisation(offreStage.getLocalisation());
        offreStageResponseDTO.setDuree(offreStage.getDuree());
        offreStageResponseDTO.setNomEntreprise(offreStage.getEntreprise().getNomEntreprise());
        offreStageResponseDTO.setCandidatures(offreStage.getCandidatures().stream()
                .map(candidature -> modelMapper.map(candidature, CandidatureInOffreStageDTO.class))
                .toList());
        return offreStageResponseDTO;
    }

    public List<OffreStageResponseDTO> offresStageToOffresStageResponseDTOs(List<OffreStage> offresStage) {
        return offresStage.stream()
                .map(this::offreStageToOffreStageResponseDTO)
                .toList();
    }
}
