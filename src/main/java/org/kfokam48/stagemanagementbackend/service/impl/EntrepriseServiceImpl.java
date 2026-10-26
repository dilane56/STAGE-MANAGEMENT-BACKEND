package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.entreprise.EntrepriseDTO;
import org.kfokam48.stagemanagementbackend.dto.entreprise.EntrepriseReponseDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.EntrepriseMapper;
import org.kfokam48.stagemanagementbackend.model.Entreprise;
import org.kfokam48.stagemanagementbackend.model.embeded.Profile;
import org.kfokam48.stagemanagementbackend.repository.EntrepriseRepository;
import org.kfokam48.stagemanagementbackend.service.EntrepriseService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.kfokam48.stagemanagementbackend.service.impl.UserAccountHelper.keepIfNull;

@Service
@Transactional
public class EntrepriseServiceImpl implements EntrepriseService {
    private final EntrepriseRepository entrepriseRepository;
    private final EntrepriseMapper entrepriseMapper;
    private final UserAccountHelper userAccountHelper;

    public EntrepriseServiceImpl(EntrepriseRepository entrepriseRepository, EntrepriseMapper entrepriseMapper,
                                 UserAccountHelper userAccountHelper) {
        this.entrepriseRepository = entrepriseRepository;
        this.entrepriseMapper = entrepriseMapper;
        this.userAccountHelper = userAccountHelper;
    }

    @Override
    public EntrepriseReponseDTO getEntrepriseById(Long id) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Entreprise not found"));

        return entrepriseMapper.entrepriseToEntrepriseReponseDTO(entreprise);
    }

    @Override
    public EntrepriseReponseDTO creerEntreprise(EntrepriseDTO entrepriseDTO) {
        Entreprise entreprise = entrepriseMapper.entrepriseDTOToEntreprise(entrepriseDTO);
        userAccountHelper.prepareNewAccount(entreprise, entrepriseDTO);
        entreprise.setRole(Roles.ENTREPRISE);
        syncProfile(entreprise);
        return entrepriseMapper.entrepriseToEntrepriseReponseDTO(entrepriseRepository.save(entreprise));
    }

    @Override
    public EntrepriseReponseDTO modifierEntreprise(Long id, EntrepriseDTO entrepriseDTO) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Entreprise not found"));

        userAccountHelper.applyCommonUpdates(entreprise, entrepriseDTO);
        entreprise.setDomaineActivite(keepIfNull(entrepriseDTO.getDomaineActivite(), entreprise.getDomaineActivite()));
        entreprise.setSiteWeb(keepIfNull(entrepriseDTO.getSiteWeb(), entreprise.getSiteWeb()));
        entreprise.setDescription(keepIfNull(entrepriseDTO.getDescription(), entreprise.getDescription()));
        entreprise.setDateCreation(keepIfNull(entrepriseDTO.getDateCreation(), entreprise.getDateCreation()));
        syncProfile(entreprise);

        entrepriseRepository.save(entreprise);
        return entrepriseMapper.entrepriseToEntrepriseReponseDTO(entreprise);
    }

    @Override
    public ResponseEntity<String> supprimerEntreprise(Long id) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Entreprise not found"));
        entrepriseRepository.delete(entreprise);
        return ResponseEntity.ok("Entreprise deleted successfully");
    }

    @Override
    public List<EntrepriseReponseDTO> getAllEntreprises() {
        return entrepriseMapper.entreprisesToEntreprisesReponseDTO(entrepriseRepository.findAll());
    }

    private void syncProfile(Entreprise entreprise) {
        Profile profile = entreprise.getProfile() != null ? entreprise.getProfile() : new Profile();
        profile.setDomaineActivite(entreprise.getDomaineActivite());
        profile.setSiteWeb(entreprise.getSiteWeb());
        profile.setDescription(entreprise.getDescription());
        profile.setDateCreation(entreprise.getDateCreation() != null ? entreprise.getDateCreation().toString() : null);
        entreprise.setProfile(profile);
    }
}
