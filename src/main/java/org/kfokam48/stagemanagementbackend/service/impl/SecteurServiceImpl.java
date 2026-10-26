package org.kfokam48.stagemanagementbackend.service.impl;

import org.kfokam48.stagemanagementbackend.dto.secteur.SecteurRequestDTO;
import org.kfokam48.stagemanagementbackend.exception.ResourceAlreadyExistException;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.mapper.SecteurMapper;
import org.kfokam48.stagemanagementbackend.model.Secteur;
import org.kfokam48.stagemanagementbackend.repository.OffreStageRepository;
import org.kfokam48.stagemanagementbackend.repository.SecteurRepository;
import org.kfokam48.stagemanagementbackend.service.SecteurService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class SecteurServiceImpl implements SecteurService {
    private final SecteurRepository secteurRepository;
    private final SecteurMapper secteurMapper;
    private final OffreStageRepository offreStageRepository;

    public SecteurServiceImpl(SecteurRepository secteurRepository, SecteurMapper secteurMapper, OffreStageRepository offreStageRepository) {
        this.secteurRepository = secteurRepository;
        this.secteurMapper = secteurMapper;
        this.offreStageRepository = offreStageRepository;
    }

    @Override
    public Secteur getSecteurById(Long id) {
        return secteurRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Secteur not found"));
    }

    @Override
    public List<Secteur> getAllSecteurs() {
        return secteurRepository.findAll();
    }

    @Override
    public Secteur saveSecteur(SecteurRequestDTO secteurRequestDTO) {
        String nom = secteurRequestDTO.getNomSecteur().trim();
        if (secteurRepository.existsByNomSecteurIgnoreCase(nom)) {
            throw new ResourceAlreadyExistException("Un secteur portant ce nom existe déjà");
        }
        secteurRequestDTO.setNomSecteur(nom);
        return secteurRepository.save(secteurMapper.secteurRequestDTOToSecteur(secteurRequestDTO));
    }

    @Override
    public String deleteSecteur(Long id) {
        Secteur secteur = secteurRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Secteur not found"));
        if (!offreStageRepository.findOffreStageBySecteur(secteur).isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer un secteur utilisé par des offres de stage");
        }
        secteurRepository.delete(secteur);
        return "Secteur deleted Successfully";
    }

    @Override
    public Secteur updateSecteur(Long id, SecteurRequestDTO secteurRequestDTO) {
        Secteur secteur = secteurRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Secteur not found"));
        String nom = secteurRequestDTO.getNomSecteur().trim();
        if (!nom.equalsIgnoreCase(secteur.getNomSecteur()) && secteurRepository.existsByNomSecteurIgnoreCase(nom)) {
            throw new ResourceAlreadyExistException("Un secteur portant ce nom existe déjà");
        }
        secteur.setNomSecteur(nom);
        secteurRepository.save(secteur);
        return secteur;
    }
}
