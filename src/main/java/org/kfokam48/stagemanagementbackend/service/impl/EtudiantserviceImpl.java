package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantDTO;
import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantResponseDTO;
import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantUpdateDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.EtudiantMapper;
import org.kfokam48.stagemanagementbackend.model.Etudiant;
import org.kfokam48.stagemanagementbackend.repository.EtudiantRepository;
import org.kfokam48.stagemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.stagemanagementbackend.service.Etudiantservice;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EtudiantserviceImpl implements Etudiantservice {
    private final EtudiantRepository etudiantRepository;
    private final EtudiantMapper etudiantMapper;
    private final UtilisateurRepository utilisateurRepository;

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
        if (utilisateurRepository.existsByUsername(etudiantDTO.getUsername())) {
            throw new RuntimeException("User already exists with this username");
        }
        Etudiant etudiant = etudiantMapper.etudiantDtoToEtudiant(etudiantDTO);
        etudiant.setRole(Roles.ETUDIANT);
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
        if (etudiantUpdateDTO.getUsername() != null && !etudiantUpdateDTO.getUsername().equals(etudiant.getUsername())) {
            if (utilisateurRepository.existsByUsername(etudiantUpdateDTO.getUsername())) {
                throw new RessourceNotFoundException("User already exists with this username");
            }
        }
        etudiant.setRole(Roles.ETUDIANT);
        etudiant.setEmail(etudiantUpdateDTO.getEmail());
        etudiant.setUsername(etudiantUpdateDTO.getUsername());
        etudiant.setNom(etudiantUpdateDTO.getNom());
        etudiant.setPrenom(etudiantUpdateDTO.getPrenom());
        etudiant.setTelephone(etudiantUpdateDTO.getTelephone());
        etudiant.setDateNaissance(etudiantUpdateDTO.getDateNaissance());
        etudiant.setAnneeScolaire(etudiantUpdateDTO.getAnneeScolaire());
        etudiant.setFiliere(etudiantUpdateDTO.getFiliere());
        etudiant.setNiveau(etudiantUpdateDTO.getNiveau());
        etudiant.setAdresse(etudiantUpdateDTO.getAdresse());
        etudiant.setPassword(etudiantUpdateDTO.getPassword());
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
