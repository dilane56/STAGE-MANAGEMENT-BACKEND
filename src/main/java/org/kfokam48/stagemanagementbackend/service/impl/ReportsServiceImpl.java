package org.kfokam48.stagemanagementbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kfokam48.stagemanagementbackend.dto.reports.ActiveUsersDTO;
import org.kfokam48.stagemanagementbackend.dto.reports.CompanyPartnersDTO;
import org.kfokam48.stagemanagementbackend.dto.reports.StagesByFiliereDTO;
import org.kfokam48.stagemanagementbackend.enums.StatutConvention;
import org.kfokam48.stagemanagementbackend.enums.UserStatus;
import org.kfokam48.stagemanagementbackend.model.ConventionStage;
import org.kfokam48.stagemanagementbackend.model.Utilisateur;
import org.kfokam48.stagemanagementbackend.repository.CandidatureRepository;
import org.kfokam48.stagemanagementbackend.repository.ConventionStageRepository;
import org.kfokam48.stagemanagementbackend.repository.EntrepriseRepository;
import org.kfokam48.stagemanagementbackend.repository.OffreStageRepository;
import org.kfokam48.stagemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.stagemanagementbackend.service.ReportsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportsServiceImpl implements ReportsService {

    private static final String NON_RENSEIGNEE = "Non renseignée";

    private final ConventionStageRepository conventionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final OffreStageRepository offreStageRepository;
    private final CandidatureRepository candidatureRepository;

    /**
     * Calcul en mémoire à partir d'une seule requête (auparavant 2 requêtes par filière).
     * En cours = approuvée et date de fin non dépassée ; terminé = approuvée et date de fin passée
     * (ce champ n'était jamais renseigné, ce qui faisait planter l'export Excel).
     */
    @Override
    public List<StagesByFiliereDTO> getStagesByFiliere() {
        LocalDate today = LocalDate.now();
        Map<String, List<ConventionStage>> parFiliere = conventionRepository.findAll().stream()
                .collect(Collectors.groupingBy(convention -> Objects.requireNonNullElse(
                        convention.getCandidature().getEtudiant().getFiliere(), NON_RENSEIGNEE)));

        return parFiliere.entrySet().stream()
                .map(entry -> {
                    List<ConventionStage> conventions = entry.getValue();
                    StagesByFiliereDTO dto = new StagesByFiliereDTO();
                    dto.setFiliere(entry.getKey());
                    dto.setTotalStages((long) conventions.size());
                    dto.setStagesEnCours(conventions.stream()
                            .filter(c -> c.getStatutConvention() == StatutConvention.APPROUVE)
                            .filter(c -> c.getDateFin() == null || !c.getDateFin().isBefore(today))
                            .count());
                    dto.setStagesTermines(conventions.stream()
                            .filter(c -> c.getStatutConvention() == StatutConvention.APPROUVE)
                            .filter(c -> c.getDateFin() != null && c.getDateFin().isBefore(today))
                            .count());
                    dto.setStagesEnAttente(conventions.stream()
                            .filter(c -> c.getStatutConvention() == StatutConvention.EN_ATTENTE
                                    || c.getStatutConvention() == StatutConvention.VALIDE)
                            .count());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportStagesByFiliereToExcel() {
        List<StagesByFiliereDTO> data = getStagesByFiliere();
        return buildWorkbook(workbook -> {
            Sheet sheet = workbook.createSheet("Stages par Filière");
            header(sheet, "Filière", "Total Stages", "En Cours", "Terminés", "En Attente");
            int rowNum = 1;
            for (StagesByFiliereDTO dto : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getFiliere());
                row.createCell(1).setCellValue(nz(dto.getTotalStages()));
                row.createCell(2).setCellValue(nz(dto.getStagesEnCours()));
                row.createCell(3).setCellValue(nz(dto.getStagesTermines()));
                row.createCell(4).setCellValue(nz(dto.getStagesEnAttente()));
            }
        });
    }

    @Override
    public List<ActiveUsersDTO> getActiveUsers() {
        return utilisateurRepository.findByStatus(UserStatus.EN_LIGNE).stream()
                .map(user -> {
                    ActiveUsersDTO dto = new ActiveUsersDTO();
                    dto.setId(user.getId());
                    dto.setFullName(user.getFullName());
                    dto.setEmail(user.getEmail());
                    dto.setRole(user.getRole());
                    dto.setDerniereConnexion(user.getDerniereConnexion());
                    dto.setStatus(user.getStatus().toString());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportActiveUsersToExcel() {
        List<ActiveUsersDTO> data = getActiveUsers();
        return buildWorkbook(workbook -> {
            Sheet sheet = workbook.createSheet("Utilisateurs Actifs");
            header(sheet, "ID", "Nom Complet", "Email", "Rôle", "Dernière Connexion", "Statut");
            int rowNum = 1;
            for (ActiveUsersDTO dto : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getId());
                row.createCell(1).setCellValue(str(dto.getFullName()));
                row.createCell(2).setCellValue(dto.getEmail());
                row.createCell(3).setCellValue(str(dto.getRole()));
                row.createCell(4).setCellValue(str(dto.getDerniereConnexion()));
                row.createCell(5).setCellValue(str(dto.getStatus()));
            }
        });
    }

    @Override
    public List<CompanyPartnersDTO> getCompanyPartners() {
        return entrepriseRepository.findAll().stream()
                .map(entreprise -> {
                    CompanyPartnersDTO dto = new CompanyPartnersDTO();
                    dto.setId(entreprise.getId());
                    dto.setFullName(entreprise.getFullName());
                    dto.setEmail(entreprise.getEmail());
                    // Lecture directe sur l'entité : le profil embarqué pouvait être null (NullPointerException)
                    dto.setDomaineActivite(entreprise.getDomaineActivite());
                    dto.setSiteWeb(entreprise.getSiteWeb());
                    dto.setTotalOffres(offreStageRepository.countByEntrepriseId(entreprise.getId()));
                    dto.setTotalCandidatures(candidatureRepository.countByOffreStageEntrepriseId(entreprise.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportCompanyPartnersToExcel() {
        List<CompanyPartnersDTO> data = getCompanyPartners();
        return buildWorkbook(workbook -> {
            Sheet sheet = workbook.createSheet("Partenaires Entreprises");
            header(sheet, "ID", "Nom Entreprise", "Email", "Domaine d'Activité", "Site Web", "Total Offres", "Total Candidatures");
            int rowNum = 1;
            for (CompanyPartnersDTO dto : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getId());
                row.createCell(1).setCellValue(str(dto.getFullName()));
                row.createCell(2).setCellValue(dto.getEmail());
                row.createCell(3).setCellValue(str(dto.getDomaineActivite()));
                row.createCell(4).setCellValue(str(dto.getSiteWeb()));
                row.createCell(5).setCellValue(nz(dto.getTotalOffres()));
                row.createCell(6).setCellValue(nz(dto.getTotalCandidatures()));
            }
        });
    }

    /** Toutes les conventions créées pendant l'année (auparavant : uniquement celles créées le 1er janvier). */
    @Override
    public byte[] exportStagesByYear(int year) {
        List<ConventionStage> conventions = conventionRepository.findByDateCreationBetween(
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        return buildWorkbook(workbook -> {
            Sheet sheet = workbook.createSheet("Stages " + year);
            header(sheet, "Étudiant", "Entreprise", "Titre Stage", "Date Début", "Date Fin", "Statut");
            int rowNum = 1;
            for (ConventionStage convention : conventions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(str(convention.getCandidature().getEtudiant().getFullName()));
                row.createCell(1).setCellValue(str(convention.getCandidature().getOffreStage().getEntreprise().getFullName()));
                row.createCell(2).setCellValue(str(convention.getCandidature().getOffreStage().getIntitule()));
                row.createCell(3).setCellValue(str(convention.getDateDebut()));
                row.createCell(4).setCellValue(str(convention.getDateFin()));
                row.createCell(5).setCellValue(str(convention.getStatutConvention()));
            }
        });
    }

    @Override
    public byte[] exportSignedConventions() {
        List<ConventionStage> conventions = conventionRepository.findByStatutConvention(StatutConvention.APPROUVE);
        return buildWorkbook(workbook -> {
            Sheet sheet = workbook.createSheet("Conventions Signées");
            header(sheet, "Étudiant", "Entreprise", "Titre Stage", "Date Signature", "Enseignant Valideur");
            int rowNum = 1;
            for (ConventionStage convention : conventions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(str(convention.getCandidature().getEtudiant().getFullName()));
                row.createCell(1).setCellValue(str(convention.getCandidature().getOffreStage().getEntreprise().getFullName()));
                row.createCell(2).setCellValue("Convention de stage " + str(convention.getCandidature().getOffreStage().getIntitule()));
                row.createCell(3).setCellValue(str(convention.getDateAprouval()));
                row.createCell(4).setCellValue(convention.getEnseignantValideur() != null ? str(convention.getEnseignantValideur().getFullName()) : "");
            }
        });
    }

    @Override
    public byte[] exportAllUsers() {
        List<Utilisateur> users = utilisateurRepository.findAll();
        return buildWorkbook(workbook -> {
            Sheet sheet = workbook.createSheet("Tous les Utilisateurs");
            header(sheet, "ID", "Nom Complet", "Email", "Rôle", "Date Création", "Statut");
            int rowNum = 1;
            for (Utilisateur user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(str(user.getFullName()));
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(str(user.getRole()));
                row.createCell(4).setCellValue(str(user.getCreateAt()));
                row.createCell(5).setCellValue(str(user.getStatus()));
            }
        });
    }

    @Override
    public byte[] exportAllData() {
        List<Utilisateur> users = utilisateurRepository.findAll();
        List<ConventionStage> conventions = conventionRepository.findAll();
        return buildWorkbook(workbook -> {
            Sheet usersSheet = workbook.createSheet("Utilisateurs");
            header(usersSheet, "ID", "Nom", "Email", "Rôle");
            int userRowNum = 1;
            for (Utilisateur user : users) {
                Row row = usersSheet.createRow(userRowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(str(user.getFullName()));
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(str(user.getRole()));
            }

            Sheet conventionsSheet = workbook.createSheet("Conventions");
            header(conventionsSheet, "Étudiant", "Entreprise", "Statut");
            int convRowNum = 1;
            for (ConventionStage convention : conventions) {
                Row row = conventionsSheet.createRow(convRowNum++);
                row.createCell(0).setCellValue(str(convention.getCandidature().getEtudiant().getFullName()));
                row.createCell(1).setCellValue(str(convention.getCandidature().getOffreStage().getEntreprise().getFullName()));
                row.createCell(2).setCellValue(str(convention.getStatutConvention()));
            }
        });
    }

    private static byte[] buildWorkbook(Consumer<Workbook> content) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            content.accept(workbook);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Erreur lors de la génération du fichier Excel", e);
        }
    }

    private static void header(Sheet sheet, String... titles) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < titles.length; i++) {
            headerRow.createCell(i).setCellValue(titles[i]);
        }
    }

    private static String str(Object value) {
        return value == null ? "" : value.toString();
    }

    private static double nz(Long value) {
        return value == null ? 0 : value;
    }
}
