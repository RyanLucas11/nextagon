package br.com.nextagon.service;

import br.com.nextagon.dto.request.AthleteProfileRequestDto;
import br.com.nextagon.dto.request.ProfessionalProfileRequestDto;
import br.com.nextagon.model.AthleteProfile;
import br.com.nextagon.model.ProfessionalProfile;
import br.com.nextagon.model.Role;
import br.com.nextagon.model.User;
import br.com.nextagon.repository.ProfessionalProfileRepository;
import br.com.nextagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;

    // ─── Athlete ──────────────────────────────────────────────────────────────

    @Transactional
    public AthleteProfile saveAthleteProfile(String userId, AthleteProfileRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (user.getRole() != Role.ATHLETE) {
            throw new SecurityException("Apenas atletas podem criar perfil de atleta");
        }

        AthleteProfile profile = user.getAthleteProfile();

        if (profile == null) {
            // Criação
            profile = AthleteProfile.builder()
                    .user(user)
                    .build();
        }

        // Atualiza campos
        profile.setHeight(dto.getHeight());
        profile.setWeight(dto.getWeight());
        profile.setHealthNotes(dto.getHealthNotes());
        profile.setFitnessLevel(dto.getFitnessLevel());

        if (dto.getGoals() != null) {
            profile.getGoals().clear();
            profile.getGoals().addAll(dto.getGoals());
        }

        // Atualiza nome de exibição no User
        user.setName(dto.getDisplayName());
        userRepository.save(user);

        return profile;
    }

    public AthleteProfile getAthleteProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (user.getAthleteProfile() == null) {
            throw new IllegalStateException("Perfil de atleta não encontrado");
        }

        return user.getAthleteProfile();
    }

    // ─── Professional ──────────────────────────────────────────────────────────

    @Transactional
    public ProfessionalProfile saveProfessionalProfile(String userId, ProfessionalProfileRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (user.getRole() != Role.PROFESSIONAL) {
            throw new SecurityException("Apenas profissionais podem criar perfil profissional");
        }

        ProfessionalProfile profile = professionalProfileRepository
                .findByUserId(userId)
                .orElseGet(() -> ProfessionalProfile.builder().user(user).build());

        profile.setBio(dto.getBio());
        profile.setHourlyRate(dto.getHourlyRate());

        if (dto.getAvailable() != null) {
            profile.setAvailable(dto.getAvailable());
        }

        if (dto.getSpecialties() != null) {
            profile.getSpecialties().clear();
            profile.getSpecialties().addAll(dto.getSpecialties());
        }

        if (dto.getCertificates() != null) {
            profile.getCertificates().clear();
            profile.getCertificates().addAll(dto.getCertificates());
        }

        user.setName(dto.getDisplayName());
        userRepository.save(user);

        return professionalProfileRepository.save(profile);
    }

    public ProfessionalProfile getProfessionalProfile(String userId) {
        return professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Perfil profissional não encontrado"));
    }
}