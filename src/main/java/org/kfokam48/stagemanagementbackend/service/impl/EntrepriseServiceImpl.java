package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.entreprise.EntrepriseDTO;
import org.kfokam48.stagemanagementbackend.dto.entreprise.EntrepriseReponseDTO;
import org.kfokam48.stagemanagementbackend.dto.entreprise.EntrepriseUpdateDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.EntrepriseMapper;
import org.kfokam48.stagemanagementbackend.model.Entreprise;
import org.kfokam48.stagemanagementbackend.repository.EntrepriseRepository;
import org.kfokam48.stagemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.stagemanagementbackend.service.EntrepriseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EntrepriseServiceImpl implements EntrepriseService {
    private final EntrepriseRepository entrepriseRepository;
    private final EntrepriseMapper entrepriseMapper;
    private final UtilisateurRepository utilisateurRepository;
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public EntrepriseServiceImpl(EntrepriseRepository entrepriseRepository, EntrepriseMapper entrepriseMapper, UtilisateurRepository utilisateurRepository) {
        this.entrepriseRepository = entrepriseRepository;
        this.entrepriseMapper = entrepriseMapper;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public EntrepriseReponseDTO getEntrepriseById(Long id) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Entreprise not found"));

        return entrepriseMapper.entrepriseToEntrepriseReponseDTO(entreprise);
    }

    @Override
    public EntrepriseReponseDTO creerEntreprise(EntrepriseDTO entrepriseDTO) {
        if (utilisateurRepository.existsByEmail(entrepriseDTO.getEmail())) {
            throw new RuntimeException("User already exists with this email");
        }
        if (utilisateurRepository.existsByUsername(entrepriseDTO.getUsername())) {
            throw new RuntimeException("User already exists with this username");
        }
        Entreprise entreprise = entrepriseMapper.entrepriseDTOToEntreprise(entrepriseDTO);
        System.out.println(entreprise);
        entreprise.setRole(Roles.ENTREPRISE);
        entreprise.setPassword(passwordEncoder().encode(entrepriseDTO.getPassword()));

        return entrepriseMapper.entrepriseToEntrepriseReponseDTO(entrepriseRepository.save(entreprise));
    }

    @Override
    public EntrepriseReponseDTO modifierEntreprise(Long id, EntrepriseUpdateDTO entrepriseUpdateDTO) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Entreprise not found"));
        if (entrepriseUpdateDTO.getEmail() !=null && !entrepriseUpdateDTO.getEmail().equals(entreprise.getEmail())) {
            if (utilisateurRepository.existsByEmail(entrepriseUpdateDTO.getEmail())) {
                throw new RuntimeException("User already exists with this email");
            }
        }
        if (entrepriseUpdateDTO.getUsername() !=null && !entrepriseUpdateDTO.getUsername().equals(entreprise.getUsername())) {
            if (utilisateurRepository.existsByUsername(entrepriseUpdateDTO.getUsername())) {
                throw new RuntimeException("User already exists with this username");
            }
        }
        entreprise.setNomEntreprise(entrepriseUpdateDTO.getNomEntreprise());
        entreprise.setSecteurActivite(entrepriseUpdateDTO.getSecteurActivite());
        entreprise.setSiteWeb(entrepriseUpdateDTO.getSiteWeb());
        entreprise.setDescription(entrepriseUpdateDTO.getDescription());
        entreprise.setEmail(entrepriseUpdateDTO.getEmail());
        entreprise.setUsername(entrepriseUpdateDTO.getUsername());
        entreprise.setPassword(passwordEncoder().encode(entrepriseUpdateDTO.getPassword()));
        entreprise.setRole(Roles.ENTREPRISE);
        entreprise.setTelephone(entrepriseUpdateDTO.getTelephone());
        entreprise.setAdresse(entrepriseUpdateDTO.getAdresse());
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
}
