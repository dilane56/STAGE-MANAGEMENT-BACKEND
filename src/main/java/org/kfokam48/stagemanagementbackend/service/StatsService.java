package org.kfokam48.stagemanagementbackend.service;

import org.kfokam48.stagemanagementbackend.dto.stats.*;
import java.util.List;

public interface StatsService {
    EtudiantStatsDTO getEtudiantStats(Long etudiantId);
    EntrepriseStatsDTO getEntrepriseStats(Long entrepriseId);
    EnseignantStatsDTO getEnseignantStats(Long enseignantId);
    AdminStatsDTO getAdminStats();
    List<MonthlyEvolutionDTO> getMonthlyEvolution();
    List<InternshipsBySectorDTO> getInternshipsBySector();
}