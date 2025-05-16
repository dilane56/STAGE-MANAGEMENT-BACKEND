package org.kfokam48.stagemanagementbackend.service;

import org.kfokam48.stagemanagementbackend.dto.convention.ConventionRequestDTO;
import org.kfokam48.stagemanagementbackend.dto.convention.ConventionResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ConventionService {
    public ConventionResponseDTO findByConventionId(Long conventionId);
    ConventionResponseDTO createConvention(ConventionRequestDTO conventionRequestDTO, MultipartFile file) throws Exception;

    public ConventionResponseDTO updateConvention(ConventionRequestDTO conventionRequestDTO);

    public String deleteConvention(Long conventionId);

    public List<ConventionResponseDTO> findAllConventions();

    public ConventionResponseDTO validateConventionByEnseignant(Long enseigantId);

    public ConventionResponseDTO approuveConventionByAdministrator(Long administratorId);
}
