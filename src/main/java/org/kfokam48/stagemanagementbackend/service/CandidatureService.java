package org.kfokam48.stagemanagementbackend.service;

import org.kfokam48.stagemanagementbackend.dto.CandidatureDTO;
import org.kfokam48.stagemanagementbackend.dto.CandidatureResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CandidatureService {

    public CandidatureResponseDTO getCandidatureById(Long id);
    public List<CandidatureResponseDTO> getAllCandidatures();
    public CandidatureResponseDTO addCandidature(CandidatureDTO candidatureDTO);
    public CandidatureResponseDTO updateCandidature(Long id,CandidatureDTO candidatureDTO);
    public ResponseEntity<String> deleteCandidature(Long id);
}
