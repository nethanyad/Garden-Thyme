package com.gardenthyme.models;

import java.util.List;

public class Pest {

    private String pestName;
    private String effectivePesticides;
    private List<String> targetPlants;
    private List<String> signs;

    public Pest(String pestName, String effectivePesticides) {
        this.pestName = pestName;
        this.effectivePesticides = effectivePesticides;
    }

    public Pest(String pestName, String effectivePesticides, List<String> targetPlants, List<String> signs) {
        this.pestName = pestName;
        this.effectivePesticides = effectivePesticides;
        this.targetPlants = targetPlants;
        this.signs = signs;
    }

    public String getPestName() { return pestName; }
    public void setPestName(String pestName) { this.pestName = pestName; }

    public String getEffectivePesticides() { return effectivePesticides; }
    public void setEffectivePesticides(String effectivePesticides) { this.effectivePesticides = effectivePesticides; }

    public List<String> getTargetPlants() { return targetPlants; }
    public void setTargetPlants(List<String> targetPlants) { this.targetPlants = targetPlants; }

    public List<String> getSigns() { return signs; }
    public void setSigns(List<String> signs) { this.signs = signs; }
}