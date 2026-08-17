package br.com.nextagon.dto.response;

import br.com.nextagon.model.User;
import lombok.Getter;

import java.util.List;

@Getter
public class ProfessionalResponseDto {

    private final String id;
    private final String name;
    private final String email;
    private final String bio;
    private final List<String> specialties;
    private final Double averageRating;
    private final Integer totalReviews;
    private final Double hourlyRate;
    private final boolean available;

    public ProfessionalResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        var profile = user.getProfessionalProfile();
        this.bio = profile != null ? profile.getBio() : null;
        this.specialties = profile != null ? profile.getSpecialties() : List.of();
        this.averageRating = profile != null ? profile.getAverageRating() : 0.0;
        this.totalReviews = profile != null ? profile.getTotalReviews() : 0;
        this.hourlyRate = profile != null ? profile.getHourlyRate() : null;
        this.available = profile != null && profile.isAvailable();
    }
}