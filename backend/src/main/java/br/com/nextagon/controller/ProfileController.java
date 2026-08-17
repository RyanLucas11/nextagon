package br.com.nextagon.controller;

import br.com.nextagon.dto.request.AthleteProfileRequestDto;
import br.com.nextagon.dto.request.ProfessionalProfileRequestDto;
import br.com.nextagon.dto.response.AthleteProfileResponseDto;
import br.com.nextagon.dto.response.ProfessionalResponseDto;
import br.com.nextagon.model.User;
import br.com.nextagon.repository.UserRepository;
import br.com.nextagon.security.AuthenticatedUser;
import br.com.nextagon.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserRepository userRepository;

    // ─── Athlete ──────────────────────────────────────────────────────────────

    @PutMapping("/athlete")
    @PreAuthorize("hasRole('ATHLETE')")
    public ResponseEntity<AthleteProfileResponseDto> saveAthleteProfile(
            @Valid @RequestBody AthleteProfileRequestDto dto) {

        String userId = AuthenticatedUser.getId();
        var profile = profileService.saveAthleteProfile(userId, dto);
        return ResponseEntity.ok(new AthleteProfileResponseDto(profile));
    }

    @GetMapping("/athlete")
    @PreAuthorize("hasRole('ATHLETE')")
    public ResponseEntity<AthleteProfileResponseDto> getMyAthleteProfile() {
        String userId = AuthenticatedUser.getId();
        return ResponseEntity.ok(
                new AthleteProfileResponseDto(profileService.getAthleteProfile(userId))
        );
    }

    // ─── Professional ──────────────────────────────────────────────────────────

    @PutMapping("/professional")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<ProfessionalResponseDto> saveProfessionalProfile(
            @Valid @RequestBody ProfessionalProfileRequestDto dto) {

        String userId = AuthenticatedUser.getId();
        profileService.saveProfessionalProfile(userId, dto);

        // Retorna o DTO completo via User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return ResponseEntity.ok(new ProfessionalResponseDto(user));
    }

    @GetMapping("/professional/{userId}")
    public ResponseEntity<ProfessionalResponseDto> getProfessionalProfile(
            @PathVariable String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return ResponseEntity.ok(new ProfessionalResponseDto(user));
    }
}