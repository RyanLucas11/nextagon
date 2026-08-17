package br.com.nextagon.dto.response;

import br.com.nextagon.model.DietPlan;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DietPlanResponseDto {

    private final String id;
    private final String contractId;
    private final String title;
    private final Integer totalCalories;
    private final Double proteinG;
    private final Double carbsG;
    private final Double fatsG;
    private final String notes;
    private final boolean active;
    private final LocalDateTime createdAt;

    public DietPlanResponseDto(DietPlan plan) {
        this.id = plan.getId();
        this.contractId = plan.getContract().getId();
        this.title = plan.getTitle();
        this.totalCalories = plan.getTotalCalories();
        this.proteinG = plan.getProteinG();
        this.carbsG = plan.getCarbsG();
        this.fatsG = plan.getFatsG();
        this.notes = plan.getNotes();
        this.active = plan.isActive();
        this.createdAt = plan.getCreatedAt();
    }
}