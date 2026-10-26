package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.administrateur.AdministrateurDTO;
import org.kfokam48.stagemanagementbackend.dto.administrateur.AdministrateurResponseDTO;
import org.kfokam48.stagemanagementbackend.dto.administrateur.AdministrateurUpdateDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.AdministrateurMappeur;
import org.kfokam48.stagemanagementbackend.model.Administrateur;
import org.kfokam48.stagemanagementbackend.repository.AdministrateurRepository;
import org.kfokam48.stagemanagementbackend.service.AdministrateurService;
import org.kfokam48.stagemanagementbackend.service.auth.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdministrateurServiceImpl implements AdministrateurService {
    private final AdministrateurRepository administrateurRepository;
    private final AdministrateurMappeur administrateurMappeur;
    private final UserAccountHelper userAccountHelper;
    private final CurrentUserService currentUserService;


    public AdministrateurServiceImpl(AdministrateurRepository administrateurRepository, AdministrateurMappeur administrateurMappeur,
                                     UserAccountHelper userAccountHelper, CurrentUserService currentUserService) {
        this.administrateurRepository = administrateurRepository;
        this.administrateurMappeur = administrateurMappeur;
        this.userAccountHelper = userAccountHelper;
        this.currentUserService = currentUserService;
    }

    @Override
    public AdministrateurResponseDTO getAdministrateurById(Long id) {
        Administrateur administrateur = administrateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Administrateur not found"));
        return  administrateurMappeur.administrateurToAdministareurReponseDTO(administrateur);
    }

    @Override
    public AdministrateurResponseDTO createAdministrateur(AdministrateurDTO administrateurDTO) {
        Administrateur administrateur = administrateurMappeur.adminsitrateurDTOToAdministrateur(administrateurDTO);
        userAccountHelper.prepareNewAccount(administrateur, administrateurDTO);
        administrateur.setRole(Roles.ADMIN);
        administrateurRepository.save(administrateur);
        return administrateurMappeur.administrateurToAdministareurReponseDTO(administrateur);
    }

    @Override
    public AdministrateurResponseDTO updateAdministrateur(Long id, AdministrateurUpdateDTO administrateurUpdateDTO) {
        Administrateur administrateur = administrateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Administrateur not found"));

        userAccountHelper.applyCommonUpdates(administrateur, administrateurUpdateDTO);
        administrateur.setRole(Roles.ADMIN);
        administrateurRepository.save(administrateur);
        return administrateurMappeur.administrateurToAdministareurReponseDTO(administrateur);
    }

    @Override
    public ResponseEntity<String> deleteAdministrateur(Long id) {
        Administrateur administrateur = administrateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Administrateur not found"));
        if (administrateur.getId().equals(currentUserService.getCurrentUserId())) {
            throw new IllegalStateException("Un administrateur ne peut pas supprimer son propre compte");
        }
        if (administrateurRepository.count() <= 1) {
            throw new IllegalStateException("Impossible de supprimer le dernier administrateur");
        }
        administrateurRepository.delete(administrateur);
        return ResponseEntity.ok("Administrateur deleted successfully");
    }

    @Override
    public List<AdministrateurResponseDTO> getAllAdministrateurs() {
        return administrateurMappeur.administrateurListToAdministrateurResponseDTOList(administrateurRepository.findAll());
    }
}
