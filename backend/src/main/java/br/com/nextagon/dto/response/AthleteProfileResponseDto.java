package br.com.nextagon.dto.response;

import br.com.nextagon.model.AthleteProfile;
import lombok.Getter;

import java.util.List;

@Getter
public class AthleteProfileResponseDto {

    private final String id;
    private final String userId;
    private final String displayName;
    private final Double height;
    private final Double weight;
    private final List<String> goals;
    private final String healthNotes;
    private final String fitnessLevel;

    public AthleteProfileResponseDto(AthleteProfile profile) {
        this.id = profile.getId();
        this.userId = profile.getUser().getId();
        this.displayName = profile.getUser().getName();
        this.height = profile.getHeight();
        this.weight = profile.getWeight();
        this.goals = profile.getGoals();
        this.healthNotes = profile.getHealthNotes();
        this.fitnessLevel = profile.getHealthNotes(); // campo adicionado abaixo
    }
}