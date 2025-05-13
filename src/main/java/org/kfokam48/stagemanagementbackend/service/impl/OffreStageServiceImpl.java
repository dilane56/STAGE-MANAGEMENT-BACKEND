package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.OffreStageDTO;
import org.kfokam48.stagemanagementbackend.dto.OffreStageResponseDTO;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.OffreStageMapper;
import org.kfokam48.stagemanagementbackend.model.OffreStage;
import org.kfokam48.stagemanagementbackend.repository.EntrepriseRepository;
import org.kfokam48.stagemanagementbackend.repository.OffreStageRepository;
import org.kfokam48.stagemanagementbackend.service.OffreStageService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class OffreStageServiceImpl implements OffreStageService {
    private final OffreStageRepository offreStageRepository;
    private final OffreStageMapper offreStageMapper;
    private final EntrepriseRepository entrepriseRepository;

    public OffreStageServiceImpl(OffreStageRepository offreStageRepository, OffreStageMapper offreStageMapper, EntrepriseRepository entrepriseRepository) {
        this.offreStageRepository = offreStageRepository;
        this.offreStageMapper = offreStageMapper;
        this.entrepriseRepository = entrepriseRepository;
    }

    @Override
    public OffreStageResponseDTO getOffreStageById(Long id) {
        OffreStage offreStage = offreStageRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Offre de stage not found"));

        return offreStageMapper.offreStageToOffreStageResponseDTO(offreStage);
    }

    @Override
    public ResponseEntity<String> deleteOffreStageById(Long id) {
        OffreStage offreStage = offreStageRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Offre de stage not found"));
        offreStageRepository.deleteById(id);
        return ResponseEntity.ok("Offre de stage deleted successfully");
    }

    @Override
    public OffreStageResponseDTO createOffreStage(OffreStageDTO offreStageDTO) {
        return offreStageMapper.offreStageToOffreStageResponseDTO(
                offreStageRepository.save(offreStageMapper.offreStageDTOToOffreStage(offreStageDTO)));
    }

    @Override
    public OffreStageResponseDTO updateOffreStage(Long id, OffreStageDTO offreStageDTO) {
        OffreStage offreStage = offreStageRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Offre de stage not found"));
        if (!entrepriseRepository.existsById(offreStageDTO.getEntrepriseId())) {
            throw new RessourceNotFoundException("Entreprise not found");
        }
        offreStage.setIntitule(offreStageDTO.getIntitule());
        offreStage.setDescription(offreStageDTO.getDescription());
        offreStage.setDomaine(offreStageDTO.getDomaine());
        offreStage.setLocalisation(offreStageDTO.getLocalisation());
        offreStage.setDuree(offreStageDTO.getDuree());
        offreStage.setEntreprise(entrepriseRepository.findById(offreStageDTO.getEntrepriseId())
                .orElseThrow(() -> new RessourceNotFoundException("Entreprise not found")));
        offreStageRepository.save(offreStage);
        return offreStageMapper.offreStageToOffreStageResponseDTO(offreStage);
    }

    @Override
    public List<OffreStageResponseDTO> getAllOffresStage() {
        return offreStageMapper.offresStageToOffresStageResponseDTOs(offreStageRepository.findAll());
    }
}
