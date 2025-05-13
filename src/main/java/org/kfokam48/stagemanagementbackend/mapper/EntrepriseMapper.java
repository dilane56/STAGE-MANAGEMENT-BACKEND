package org.kfokam48.stagemanagementbackend.mapper;

import org.kfokam48.stagemanagementbackend.dto.OffreStageInEntrepriseDTO;
import org.kfokam48.stagemanagementbackend.dto.entreprise.EntrepriseDTO;
import org.kfokam48.stagemanagementbackend.dto.entreprise.EntrepriseReponseDTO;
import org.kfokam48.stagemanagementbackend.model.Entreprise;
import org.kfokam48.stagemanagementbackend.repository.OffreStageRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntrepriseMapper {
    private final ModelMapper modelMapper;

    public EntrepriseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    public Entreprise entrepriseDTOToEntreprise(EntrepriseDTO entrepriseDTO) {
        return modelMapper.map(entrepriseDTO, Entreprise.class);
    }
    public EntrepriseReponseDTO entrepriseToEntrepriseReponseDTO(Entreprise entreprise) {
        EntrepriseReponseDTO entrepriseReponseDTO = new EntrepriseReponseDTO();
        entrepriseReponseDTO.setId(entreprise.getId());
        entrepriseReponseDTO.setNomEntreprise(entreprise.getNomEntreprise());
        entrepriseReponseDTO.setSecteurActivite(entreprise.getSecteurActivite());
        entrepriseReponseDTO.setEmail(entreprise.getEmail());
        entrepriseReponseDTO.setUsername(entreprise.getUsername());
        entrepriseReponseDTO.setRole(entreprise.getRole());
        entrepriseReponseDTO.setPassword(entreprise.getPassword());
        entrepriseReponseDTO.setTelephone(entreprise.getTelephone());
        entrepriseReponseDTO.setAdresse(entreprise.getAdresse());
        entrepriseReponseDTO.setSiteWeb(entreprise.getSiteWeb());
        entrepriseReponseDTO.setDescription(entreprise.getDescription());
        entrepriseReponseDTO.setOffres(entreprise.getOffres().stream()
                .map(offreStage -> modelMapper.map(offreStage, OffreStageInEntrepriseDTO.class))
                .toList());
        return entrepriseReponseDTO;
    }

    public List<EntrepriseReponseDTO> entreprisesToEntreprisesReponseDTO(List<Entreprise> entreprises) {
        return entreprises.stream()
                .map(this::entrepriseToEntrepriseReponseDTO)
                .toList();
    }

}
