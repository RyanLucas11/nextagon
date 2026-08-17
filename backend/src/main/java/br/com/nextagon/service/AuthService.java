package br.com.nextagon.service;

import br.com.nextagon.dto.AuthResponseDto;
import br.com.nextagon.dto.LoginDto;
import br.com.nextagon.dto.RegisterDto;
import br.com.nextagon.dto.UserSummaryDto;
import br.com.nextagon.model.User;
import br.com.nextagon.repository.UserRepository;
import br.com.nextagon.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    public AuthResponseDto login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!user.isActive()) {
            throw new IllegalStateException("Conta inativa");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        return buildAuthResponse(user);
    }

    private AuthResponseDto buildAuthResponse(User user) {
        String role = user.getRole().name();
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), role);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail(), role);

        UserSummaryDto userDto = new UserSummaryDto(
                user.getId(), user.getName(), user.getEmail(), user.getRole());

        return new AuthResponseDto(accessToken, refreshToken, userDto);
    }
}