package org.kfokam48.stagemanagementbackend.mapper;

import org.kfokam48.stagemanagementbackend.dto.utilisateur.Contact;
import org.kfokam48.stagemanagementbackend.dto.utilisateur.UtilisateurDTO;
import org.kfokam48.stagemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.stagemanagementbackend.model.Utilisateur;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UtilisateurMappeur {
    private final ModelMapper modelMapper;

    public UtilisateurMappeur(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    public Utilisateur  utilisateurDTOToUtilisateur(UtilisateurDTO utilisateurDTO) {
        return modelMapper.map(utilisateurDTO, Utilisateur.class);
    }
    public UtilisateurResponseDTO utilisateurToUtilisateurResponseDTO(Utilisateur utilisateur) {
        return modelMapper.map(utilisateur, UtilisateurResponseDTO.class);
    }
    public List<UtilisateurResponseDTO> utilisateursToUtilisateurResponseDTOs(List<Utilisateur> utilisateurs) {
        return utilisateurs.stream()
                .map(this::utilisateurToUtilisateurResponseDTO)
                .toList();
    }
    public Contact utilisateurToContact (Utilisateur user){
        Contact contact = new Contact();
        contact.setStatus(user.getStatus());
        contact.setId(user.getId());
        contact.setEmail(user.getEmail());
        contact.setNom(user.getFullName());
        contact.setDerniereConnexion(user.getDerniereConnexion());
        contact.setRole(user.getRole().name());
        return contact;


    }
    public List<Contact> utilisateursToContacts(List<Utilisateur> utilisateurs) {
        return utilisateurs.stream()
                .map(this::utilisateurToContact)
                .collect(Collectors.toList());
    }


}
