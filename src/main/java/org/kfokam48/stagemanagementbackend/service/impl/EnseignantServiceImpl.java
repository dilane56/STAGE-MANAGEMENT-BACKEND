package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.enseigant.EnseignantDTO;
import org.kfokam48.stagemanagementbackend.dto.enseigant.EnseignantResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.EnseignantMapper;
import org.kfokam48.stagemanagementbackend.model.Enseignant;
import org.kfokam48.stagemanagementbackend.model.embeded.Profile;
import org.kfokam48.stagemanagementbackend.repository.EnseignantRepository;
import org.kfokam48.stagemanagementbackend.service.EnseignantService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.kfokam48.stagemanagementbackend.service.impl.UserAccountHelper.keepIfNull;

@Service
@Transactional
public class EnseignantServiceImpl implements EnseignantService {
    private final EnseignantRepository enseignantRepository;
    private final EnseignantMapper enseignantMapper;
    private final UserAccountHelper userAccountHelper;

    public EnseignantServiceImpl(EnseignantRepository enseignantRepository, EnseignantMapper enseignantMapper,
                                 UserAccountHelper userAccountHelper) {
        this.enseignantRepository = enseignantRepository;
        this.enseignantMapper = enseignantMapper;
        this.userAccountHelper = userAccountHelper;
    }

    @Override
    public EnseignantResponseDTO createEnseignant(EnseignantDTO enseignantDTO) {
        Enseignant enseignant = enseignantMapper.enseigantDTOToEnseignant(enseignantDTO);
        userAccountHelper.prepareNewAccount(enseignant, enseignantDTO);
        enseignant.setRole(Roles.ENSEIGNANT);
        syncProfile(enseignant);
        return enseignantMapper.enseignantToEnseignantResponseDTO(enseignantRepository.save(enseignant));
    }

    @Override
    public EnseignantResponseDTO getEnseignantById(Long id) {
        Enseignant enseignant = enseignantRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Enseignant not found"));
        return enseignantMapper.enseignantToEnseignantResponseDTO(enseignant);
    }

    @Override
    public List<EnseignantResponseDTO> getAllEnseignants() {
        return enseignantMapper.enseigantListToEnseigantResponseDTOs(enseignantRepository.findAll());
    }

    @Override
    public EnseignantResponseDTO updateEnseignant(Long id, EnseignantDTO enseignantDTO) {
        Enseignant enseignant = enseignantRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Enseignant not found"));

        userAccountHelper.applyCommonUpdates(enseignant, enseignantDTO);
        enseignant.setFiliere(keepIfNull(enseignantDTO.getFiliere(), enseignant.getFiliere()));
        enseignant.setGrade(keepIfNull(enseignantDTO.getGrade(), enseignant.getGrade()));
        enseignant.setDepartement(keepIfNull(enseignantDTO.getDepartement(), enseignant.getDepartement()));
        enseignant.setUniversite(keepIfNull(enseignantDTO.getUniversite(), enseignant.getUniversite()));
        syncProfile(enseignant);

        enseignantRepository.save(enseignant);
        return enseignantMapper.enseignantToEnseignantResponseDTO(enseignant);
    }

    @Override
    public ResponseEntity<String> deleteEnseignant(Long id) {
        Enseignant enseignant = enseignantRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Enseignant not found"));
        enseignantRepository.delete(enseignant);
        return ResponseEntity.ok("Enseignant deleted successfully");
    }

    /** La filière, le grade et le département sont désormais aussi exposés dans le profil. */
    private void syncProfile(Enseignant enseignant) {
        Profile profile = enseignant.getProfile() != null ? enseignant.getProfile() : new Profile();
        profile.setUniversite(enseignant.getUniversite());
        profile.setFiliere(enseignant.getFiliere());
        profile.setGrade(enseignant.getGrade());
        profile.setDepartement(enseignant.getDepartement());
        enseignant.setProfile(profile);
    }
}
