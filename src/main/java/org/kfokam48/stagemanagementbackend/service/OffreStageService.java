package org.kfokam48.stagemanagementbackend.service;

import org.kfokam48.stagemanagementbackend.dto.OffreStageDTO;
import org.kfokam48.stagemanagementbackend.dto.OffreStageResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OffreStageService {
    public OffreStageResponseDTO getOffreStageById(Long id);
    public ResponseEntity<String> deleteOffreStageById(Long id);
    public OffreStageResponseDTO createOffreStage(OffreStageDTO offreStageDTO);
    List<OffreStageResponseDTO> filterOffresStage(String localisation, String duree, String domaine);
    public OffreStageResponseDTO updateOffreStage(Long id, OffreStageDTO offreStageDTO);
    public List<OffreStageResponseDTO> getAllOffresStage();

}
