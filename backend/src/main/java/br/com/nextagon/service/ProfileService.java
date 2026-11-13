
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;


    @Transactional
    public AthleteProfile saveAthleteProfile(
            String userId,
            AthleteProfileRequestDto dto
    ) {
        User user = findActiveUser(userId);

        if (user.getRole() != Role.ATHLETE) {
            throw new SecurityException(
                    "Apenas atletas podem criar perfil de atleta"
            );
        }

        AthleteProfile profile = user.getAthleteProfile();

        if (profile == null) {
            profile = AthleteProfile.builder()
                    .user(user)
                    .build();

            user.setAthleteProfile(profile);
        }

        if (dto.isHeightProvided() || dto.getHeight() != null) {
            profile.setHeight(dto.getHeight());
        }

        if (dto.isWeightProvided() || dto.getWeight() != null) {
            profile.setWeight(dto.getWeight());
        }

        if (dto.isHealthNotesProvided()
                || dto.getHealthNotes() != null) {
            profile.setHealthNotes(dto.getHealthNotes());
        }

        if (dto.isFitnessLevelProvided()
                || dto.getFitnessLevel() != null) {
            profile.setFitnessLevel(dto.getFitnessLevel());
        }

        if (dto.isGoalsProvided() || dto.getGoals() != null) {
            profile.getGoals().clear();

            if (dto.getGoals() != null) {
                profile.getGoals().addAll(dto.getGoals());
            }
        }

        user.setName(dto.getDisplayName().trim());

        userRepository.save(user);

        return profile;
    }

    @Transactional(readOnly = true)
    public AthleteProfile getAthleteProfile(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado"
                        )
                );

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Conta inativa"
            );
        }

        if (user.getRole() != Role.ATHLETE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Este usuário não é um atleta"
            );
        }

        if (user.getAthleteProfile() == null) {
            throw new IllegalStateException(
                    "Perfil de atleta não encontrado"
            );
        }

        return user.getAthleteProfile();
    }


    @Transactional
    public ProfessionalProfile saveProfessionalProfile(
            String userId,
            ProfessionalProfileRequestDto dto
    ) {
        User user = findActiveUser(userId);

        if (user.getRole() != Role.PROFESSIONAL) {
            throw new SecurityException(
                    "Apenas profissionais podem criar perfil profissional"
            );
        }

        ProfessionalProfile profile = professionalProfileRepository
                .findByUserId(userId)
                .orElseGet(() -> ProfessionalProfile.builder()
                        .user(user)
                        .build()
                );

        if (dto.isBioProvided() || dto.getBio() != null) {
            profile.setBio(dto.getBio());
        }

        if (dto.isHourlyRateProvided() || dto.getHourlyRate() != null) {
            profile.setHourlyRate(dto.getHourlyRate());
        }

        if (dto.isAvailableProvided() && dto.getAvailable() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Disponibilidade não pode ser nula"
            );
        }

        if (dto.getAvailable() != null) {
            profile.setAvailable(dto.getAvailable());
        }

        if (dto.isSpecialtiesProvided()
                || dto.getSpecialties() != null) {

            profile.getSpecialties().clear();

            if (dto.getSpecialties() != null) {
                profile.getSpecialties().addAll(dto.getSpecialties());
            }
        }

        if (dto.isCertificatesProvided()
                || dto.getCertificates() != null) {

            profile.getCertificates().clear();

            if (dto.getCertificates() != null) {
                profile.getCertificates().addAll(dto.getCertificates());
            }
        }

        user.setName(dto.getDisplayName().trim());

        userRepository.save(user);

        return professionalProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public ProfessionalProfile getProfessionalProfile(String userId) {

        return professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Perfil profissional não encontrado"
                ));
    }

    private User findActiveUser(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado"
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Conta inativa não pode editar o perfil"
            );
        }

        return user;
    }
}
