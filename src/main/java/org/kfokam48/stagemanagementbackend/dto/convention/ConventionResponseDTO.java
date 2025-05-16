package org.kfokam48.stagemanagementbackend.dto.convention;

import lombok.Data;
import org.kfokam48.stagemanagementbackend.dto.CandidatureResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.StatutCandidature;

import java.time.LocalDate;

@Data
public class ConventionResponseDTO {
    private Long idConvention;
    private String enseignantName;
    private String administratorName;
    private StatutCandidature statutConvention;
    private CandidatureResponseDTO candidature;
    private LocalDate DateCreation;
    private LocalDate DateValidation;
    private LocalDate DateAprouval;
}
