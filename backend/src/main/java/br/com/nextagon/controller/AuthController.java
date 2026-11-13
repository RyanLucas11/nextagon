
package br.com.nextagon.controller;

import br.com.nextagon.dto.AuthResponseDto;
import br.com.nextagon.dto.LoginDto;
import br.com.nextagon.dto.RegisterDto;
import br.com.nextagon.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody RegisterDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginDto dto
    ) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @Valid @RequestBody RefreshRequest dto
    ) {
        return ResponseEntity.ok(
                authService.refreshToken(dto.refreshToken())
        );
    }

    public record RefreshRequest(
            @NotBlank(message = "Refresh token é obrigatório")
            String refreshToken
    ) {
    }
}
