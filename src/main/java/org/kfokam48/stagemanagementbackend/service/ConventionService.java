package org.kfokam48.stagemanagementbackend.service;

import org.kfokam48.stagemanagementbackend.dto.convention.ConventionRequestDTO;
import org.kfokam48.stagemanagementbackend.dto.convention.ConventionResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ConventionService {
    public ConventionResponseDTO findByConventionId(Long conventionId);
    ConventionResponseDTO createConvention(ConventionRequestDTO conventionRequestDTO);
    ConventionResponseDTO updateConvention(Long conventionId, Long idCandidature);
    public String deleteConvention(Long conventionId);
    public List<ConventionResponseDTO> findAllConventions();
    public List<ConventionResponseDTO> findConventionsByEntreprise(Long entrepriseId);
    public List<ConventionResponseDTO> findConventionsByEnseignant(Long enseignantId);
    ConventionResponseDTO validateConventionByEnseignant(Long enseigantId, Long conventionId);
    ConventionResponseDTO approuveConventionByAdministrator(Long administratorId, Long conventionId);
    ResponseEntity<byte[]> generateConventionPdf(Long conventionId) throws Exception;
}
