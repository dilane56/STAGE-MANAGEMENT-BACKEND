package org.kfokam48.stagemanagementbackend.dto.stats;

public class MonthlyEvolutionDTO {
    private String month;
    private Long internships;
    private Long applications;

    public MonthlyEvolutionDTO() {}

    public MonthlyEvolutionDTO(String month, Long internships, Long applications) {
        this.month = month;
        this.internships = internships;
        this.applications = applications;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Long getInternships() {
        return internships;
    }

    public void setInternships(Long internships) {
        this.internships = internships;
    }

    public Long getApplications() {
        return applications;
    }

    public void setApplications(Long applications) {
        this.applications = applications;
    }
}