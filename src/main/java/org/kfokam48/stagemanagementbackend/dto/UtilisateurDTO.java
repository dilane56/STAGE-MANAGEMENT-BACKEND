package org.kfokam48.stagemanagementbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kfokam48.stagemanagementbackend.enums.Roles;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurDTO {
    @NotNull(message = "L'email ne doit pas être null")
    @NotBlank(message = "L'email ne doit pas être vide")
    private String email;
    @NotNull(message = "Le nom ne doit pas être null")
    @NotBlank(message = "Le nom ne doit pas être vide")
    private String password;
    @NotNull(message = "Le username ne doit pas être null")
    @NotBlank(message = "Le username ne doit pas être vide")
    private String username;
    private Roles role;
}
