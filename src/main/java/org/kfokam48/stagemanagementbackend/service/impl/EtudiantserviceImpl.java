package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantDTO;
import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.EtudiantMapper;
import org.kfokam48.stagemanagementbackend.model.Enseignant;
import org.kfokam48.stagemanagementbackend.model.Etudiant;
import org.kfokam48.stagemanagementbackend.model.embeded.Profile;
import org.kfokam48.stagemanagementbackend.repository.EnseignantRepository;
import org.kfokam48.stagemanagementbackend.repository.EtudiantRepository;
import org.kfokam48.stagemanagementbackend.service.Etudiantservice;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.kfokam48.stagemanagementbackend.service.impl.UserAccountHelper.keepIfNull;

@Service
@Transactional
public class EtudiantserviceImpl implements Etudiantservice {
    private final EtudiantRepository etudiantRepository;
    private final EtudiantMapper etudiantMapper;
    private final EnseignantRepository enseignantRepository;
    private final UserAccountHelper userAccountHelper;

    public EtudiantserviceImpl(EtudiantRepository etudiantRepository, EtudiantMapper etudiantMapper,
                               EnseignantRepository enseignantRepository, UserAccountHelper userAccountHelper) {
        this.etudiantRepository = etudiantRepository;
        this.etudiantMapper = etudiantMapper;
        this.enseignantRepository = enseignantRepository;
        this.userAccountHelper = userAccountHelper;
    }

    @Override
    public EtudiantResponseDTO createEtudiant(EtudiantDTO etudiantDTO) {
        Etudiant etudiant = etudiantMapper.etudiantDtoToEtudiant(etudiantDTO);
        userAccountHelper.prepareNewAccount(etudiant, etudiantDTO);
        etudiant.setRole(Roles.ETUDIANT);
        syncProfile(etudiant);
        return etudiantMapper.etudiantToEtudiantResponseDTO(etudiantRepository.save(etudiant));
    }

    @Override
    public EtudiantResponseDTO getEtudiantById(Long id) {
       Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Etudiant not found"));
        return etudiantMapper.etudiantToEtudiantResponseDTO(etudiant);
    }

    @Override
    public List<EtudiantResponseDTO> getAllEtudiants() {
        return etudiantMapper.etudiantsToEtudiantResponseDTOs(etudiantRepository.findAll());
    }

    @Override
    public List<EtudiantResponseDTO> getEtudiantsByEnseignant(Long enseignantId) {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RessourceNotFoundException("Enseignant introuvable avec l'ID: " + enseignantId));

        List<Etudiant> etudiants = etudiantRepository.findByFiliere(enseignant.getFiliere());
        return etudiantMapper.etudiantsToEtudiantResponseDTOs(etudiants);
    }

    @Override
    public EtudiantResponseDTO updateEtudiant(Long id, EtudiantDTO etudiantDTO) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Etudiant not found"));

        userAccountHelper.applyCommonUpdates(etudiant, etudiantDTO);
        etudiant.setAnneeScolaire(keepIfNull(etudiantDTO.getAnneeScolaire(), etudiant.getAnneeScolaire()));
        etudiant.setFiliere(keepIfNull(etudiantDTO.getFiliere(), etudiant.getFiliere()));
        etudiant.setNiveau(keepIfNull(etudiantDTO.getNiveau(), etudiant.getNiveau()));
        etudiant.setUniversite(keepIfNull(etudiantDTO.getUniversite(), etudiant.getUniversite()));
        syncProfile(etudiant);

        etudiantRepository.save(etudiant);
        return etudiantMapper.etudiantToEtudiantResponseDTO(etudiant);
    }

    @Override
    public ResponseEntity<String> deleteEtudiant(Long id) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Etudiant not found"));
        etudiantRepository.delete(etudiant);
        return ResponseEntity.ok("Etudiant deleted successfully");
    }

    /** Recopie les champs spécifiques dans le profil embarqué lu par le frontend. */
    private void syncProfile(Etudiant etudiant) {
        Profile profile = etudiant.getProfile() != null ? etudiant.getProfile() : new Profile();
        profile.setFiliere(etudiant.getFiliere());
        profile.setAnneeScolaire(etudiant.getAnneeScolaire());
        profile.setNiveau(etudiant.getNiveau());
        profile.setUniversite(etudiant.getUniversite());
        etudiant.setProfile(profile);
    }
}
