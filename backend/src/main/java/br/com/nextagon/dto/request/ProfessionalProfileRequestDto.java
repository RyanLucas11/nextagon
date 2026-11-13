
package br.com.nextagon.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProfessionalProfileRequestDto {

    @NotBlank(message = "Nome de exibição é obrigatório")
    @Size(max = 150)
    private String displayName;

    @Size(max = 2000)
    private String bio;

    private List<String> specialties;

    private List<String> certificates;

    @DecimalMin(
            value = "0.0",
            message = "Valor hora não pode ser negativo"
    )
    private Double hourlyRate;

    private Boolean available;

    private boolean bioProvided;
    private boolean specialtiesProvided;
    private boolean certificatesProvided;
    private boolean hourlyRateProvided;
    private boolean availableProvided;

    @JsonSetter("bio")
    public void setBio(String bio) {
        this.bio = bio;
        this.bioProvided = true;
    }

    @JsonSetter("specialties")
    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
        this.specialtiesProvided = true;
    }

    @JsonSetter("certificates")
    public void setCertificates(List<String> certificates) {
        this.certificates = certificates;
        this.certificatesProvided = true;
    }

    @JsonSetter("hourlyRate")
    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
        this.hourlyRateProvided = true;
    }

    @JsonSetter("available")
    public void setAvailable(Boolean available) {
        this.available = available;
        this.availableProvided = true;
    }
}
