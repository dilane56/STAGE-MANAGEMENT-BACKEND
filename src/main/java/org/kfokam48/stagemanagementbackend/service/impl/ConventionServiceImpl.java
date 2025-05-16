package org.kfokam48.stagemanagementbackend.service.impl;

import jakarta.transaction.Transactional;
import org.kfokam48.stagemanagementbackend.dto.convention.ConventionRequestDTO;
import org.kfokam48.stagemanagementbackend.dto.convention.ConventionResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.StatutCandidature;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.ConventionMapper;
import org.kfokam48.stagemanagementbackend.minio.MinIOService;
import org.kfokam48.stagemanagementbackend.model.ConventionStage;
import org.kfokam48.stagemanagementbackend.repository.ConventionStageRepository;
import org.kfokam48.stagemanagementbackend.service.ConventionService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
@Service
@Transactional
public class ConventionServiceImpl implements ConventionService {
    private final ConventionStageRepository conventionStageRepository;
    private final ConventionMapper conventionMapper;
    private final MinIOService minIOService;

    public ConventionServiceImpl(ConventionStageRepository conventionStageRepository, ConventionMapper conventionMapper, MinIOService minIOService) {
        this.conventionStageRepository = conventionStageRepository;
        this.conventionMapper = conventionMapper;
        this.minIOService = minIOService;
    }

    @Override
    public ConventionResponseDTO findByConventionId(Long conventionId) {
        ConventionStage conventionStage = conventionStageRepository.findById(conventionId).orElseThrow(()-> new RessourceNotFoundException("Convention not Found"));
        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }

    @Override
    public ConventionResponseDTO createConvention(ConventionRequestDTO conventionRequestDTO, MultipartFile file) throws Exception {
        ConventionStage conventionStage = conventionMapper.conventionRequestDTOToCoventionStage(conventionRequestDTO);
        conventionStage.setStatutConvention(StatutCandidature.EN_ATTENTE);
        conventionStage.setDateCreation(LocalDate.now());
        // ✅ Upload du fichier vers MinIO
        String fileUrl = minIOService.uploadFile(file);
        conventionStage.setPdfConventionPath(fileUrl);


        conventionStageRepository.save(conventionStage);
        return conventionMapper.conventionStageToConventionResponseDTO(conventionStage);
    }
    @Override
    public ConventionResponseDTO updateConvention(ConventionRequestDTO conventionRequestDTO) {
        return null;
    }

    @Override
    public String deleteConvention(Long conventionId) {
        return "";
    }

    @Override
    public List<ConventionResponseDTO> findAllConventions() {
        return List.of();
    }

    @Override
    public ConventionResponseDTO validateConventionByEnseignant(Long enseigantId) {
        return null;
    }

    @Override
    public ConventionResponseDTO approuveConventionByAdministrator(Long administratorId) {
        return null;
    }
}
