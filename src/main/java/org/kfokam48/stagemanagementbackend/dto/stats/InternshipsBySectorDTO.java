package org.kfokam48.stagemanagementbackend.dto.stats;

public class InternshipsBySectorDTO {
    private String name;
    private Long value;
    private String color;

    public InternshipsBySectorDTO() {}

    public InternshipsBySectorDTO(String name, Long value, String color) {
        this.name = name;
        this.value = value;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}