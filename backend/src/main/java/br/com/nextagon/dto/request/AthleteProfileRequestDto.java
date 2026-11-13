
package br.com.nextagon.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AthleteProfileRequestDto {

    @NotBlank(message = "Nome de exibição é obrigatório")
    @Size(max = 150)
    private String displayName;

    @DecimalMin(value = "0.50")
    @DecimalMax(value = "2.50")
    private Double height;

    @DecimalMin(value = "20.0")
    @DecimalMax(value = "500.0")
    private Double weight;

    private List<String> goals;

    @Size(max = 2000)
    private String healthNotes;

    @Size(max = 50)
    private String fitnessLevel;

    private boolean heightProvided;
    private boolean weightProvided;
    private boolean goalsProvided;
    private boolean healthNotesProvided;
    private boolean fitnessLevelProvided;

    @JsonSetter("height")
    public void setHeight(Double height) {
        this.height = height;
        this.heightProvided = true;
    }

    @JsonSetter("weight")
    public void setWeight(Double weight) {
        this.weight = weight;
        this.weightProvided = true;
    }

    @JsonSetter("goals")
    public void setGoals(List<String> goals) {
        this.goals = goals;
        this.goalsProvided = true;
    }

    @JsonSetter("healthNotes")
    public void setHealthNotes(String healthNotes) {
        this.healthNotes = healthNotes;
        this.healthNotesProvided = true;
    }

    @JsonSetter("fitnessLevel")
    public void setFitnessLevel(String fitnessLevel) {
        this.fitnessLevel = fitnessLevel;
        this.fitnessLevelProvided = true;
    }
}
