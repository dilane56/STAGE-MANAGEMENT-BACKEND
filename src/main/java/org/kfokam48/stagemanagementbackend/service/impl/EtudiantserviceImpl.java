package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantDTO;
import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantResponseDTO;
import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantUpdateDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.EtudiantMapper;
import org.kfokam48.stagemanagementbackend.model.Etudiant;
import org.kfokam48.stagemanagementbackend.model.embeded.Profile;
import org.kfokam48.stagemanagementbackend.repository.EtudiantRepository;
import org.kfokam48.stagemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.stagemanagementbackend.service.Etudiantservice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EtudiantserviceImpl implements Etudiantservice {
    private final EtudiantRepository etudiantRepository;
    private final EtudiantMapper etudiantMapper;
    private final UtilisateurRepository utilisateurRepository;
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public EtudiantserviceImpl(EtudiantRepository etudiantRepository, EtudiantMapper etudiantMapper, UtilisateurRepository utilisateurRepository) {
        this.etudiantRepository = etudiantRepository;
        this.etudiantMapper = etudiantMapper;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public EtudiantResponseDTO createEtudiant(EtudiantDTO etudiantDTO) {
        if (utilisateurRepository.existsByEmail(etudiantDTO.getEmail())) {
            throw new RuntimeException("User already exists with this email");
        }
        Etudiant etudiant = etudiantMapper.etudiantDtoToEtudiant(etudiantDTO);
        etudiant.setRole(Roles.ETUDIANT);
        etudiant.setPassword(passwordEncoder().encode(etudiantDTO.getPassword()));
        
        // Remplir le Profile avec les informations spécifiques à l'étudiant
        Profile profile = new Profile();
        profile.setFiliere(etudiant.getFiliere());
        profile.setAnneeScolaire(etudiant.getAnneeScolaire());
        profile.setNiveau(etudiant.getNiveau());
        profile.setUniversite(etudiant.getUniversite());
        etudiant.setProfile(profile);
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
    public EtudiantResponseDTO updateEtudiant(Long id, EtudiantUpdateDTO etudiantUpdateDTO) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Etudiant not found"));
        if (etudiantUpdateDTO.getEmail() != null && !etudiantUpdateDTO.getEmail().equals(etudiant.getEmail())) {
            if (utilisateurRepository.existsByEmail(etudiantUpdateDTO.getEmail())) {
                throw new RessourceNotFoundException("User already exists with this email");
            }
        }
        etudiant.setRole(Roles.ETUDIANT);
        etudiant.setEmail(etudiantUpdateDTO.getEmail());
        etudiant.setFullName(etudiantUpdateDTO.getFullName());
        etudiant.setTelephone(etudiantUpdateDTO.getTelephone());
        etudiant.setAvatar(etudiantUpdateDTO.getAvatar());
        etudiant.setAnneeScolaire(etudiantUpdateDTO.getAnneeScolaire());
        etudiant.setFiliere(etudiantUpdateDTO.getFiliere());
        etudiant.setNiveau(etudiantUpdateDTO.getNiveau());
        etudiant.setUniversite(etudiantUpdateDTO.getUniversite());
        etudiant.setPassword(passwordEncoder().encode(etudiantUpdateDTO.getPassword()));
        
        // Mettre à jour le Profile
        Profile profile = etudiant.getProfile() != null ? etudiant.getProfile() : new Profile();
        profile.setFiliere(etudiant.getFiliere());
        profile.setAnneeScolaire(etudiant.getAnneeScolaire());
        profile.setNiveau(etudiant.getNiveau());
        profile.setUniversite(etudiant.getUniversite());
        etudiant.setProfile(profile);
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
}
