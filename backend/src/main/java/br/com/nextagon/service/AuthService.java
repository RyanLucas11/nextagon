
package br.com.nextagon.service;

import br.com.nextagon.dto.AuthResponseDto;
import br.com.nextagon.dto.LoginDto;
import br.com.nextagon.dto.RegisterDto;
import br.com.nextagon.dto.UserSummaryDto;
import br.com.nextagon.model.ProfessionalProfile;
import br.com.nextagon.model.Role;
import br.com.nextagon.model.User;
import br.com.nextagon.repository.UserRepository;
import br.com.nextagon.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponseDto register(RegisterDto dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if (dto.getRole() == Role.ADMIN) {
            throw new SecurityException(
                    "Não é permitido criar uma conta de administrador por este endpoint"
            );
        }

        if (dto.getRole() != Role.ATHLETE
                && dto.getRole() != Role.PROFESSIONAL) {
            throw new IllegalArgumentException(
                    "Tipo de usuário inválido"
            );
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .active(true)
                .build();

        if (dto.getRole() == Role.PROFESSIONAL) {

            ProfessionalProfile profile = ProfessionalProfile.builder()
                    .user(user)
                    .available(true)
                    .averageRating(0.0)
                    .totalReviews(0)
                    .build();

            user.setProfessionalProfile(profile);
        }

        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginDto dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Credenciais inválidas")
                );

        if (!user.isActive()) {
            throw new IllegalStateException("Conta inativa");
        }

        if (!passwordEncoder.matches(
                dto.getPassword(),
                user.getPassword()
        )) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto refreshToken(String refreshToken) {

        if (refreshToken == null
                || refreshToken.isBlank()
                || !jwtUtil.validateRefreshToken(refreshToken)) {
            throw invalidRefreshToken();
        }

        String userId = jwtUtil.extractUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(this::invalidRefreshToken);

        if (!user.isActive()) {
            throw invalidRefreshToken();
        }

        return buildAuthResponse(user);
    }

    private ResponseStatusException invalidRefreshToken() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Refresh token inválido ou expirado"
        );
    }

    private AuthResponseDto buildAuthResponse(User user) {

        String role = user.getRole().name();

        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                role
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId(),
                user.getEmail(),
                role
        );

        UserSummaryDto userDto = new UserSummaryDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                userDto
        );
    }
}
