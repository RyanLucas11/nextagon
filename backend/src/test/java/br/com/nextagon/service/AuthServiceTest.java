
package br.com.nextagon.service;

import br.com.nextagon.dto.AuthResponseDto;
import br.com.nextagon.model.Role;
import br.com.nextagon.model.User;
import br.com.nextagon.repository.UserRepository;
import br.com.nextagon.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String USER_ID = "user-1";
    private static final String EMAIL = "atleta@teste.com";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String ACCESS_TOKEN = "access-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User buildUser(boolean active) {
        User user = new User();
        user.setId(USER_ID);
        user.setName("Atleta Teste");
        user.setEmail(EMAIL);
        user.setRole(Role.ATHLETE);
        user.setActive(active);
        return user;
    }

    @Test
    @DisplayName("Deve renovar tokens quando o refresh token for válido")
    void deveRenovarTokensValidos() {
        User user = buildUser(true);

        when(jwtUtil.validateRefreshToken(REFRESH_TOKEN))
                .thenReturn(true);

        when(jwtUtil.extractUserId(REFRESH_TOKEN))
                .thenReturn(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateAccessToken(
                USER_ID, EMAIL, "ATHLETE"
        )).thenReturn(ACCESS_TOKEN);

        when(jwtUtil.generateRefreshToken(
                USER_ID, EMAIL, "ATHLETE"
        )).thenReturn("novo-refresh-token");

        AuthResponseDto response =
                authService.refreshToken(REFRESH_TOKEN);

        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.getAccessToken());
        assertEquals(
                "novo-refresh-token",
                response.getRefreshToken()
        );
        assertEquals(USER_ID, response.getUser().getId());

        verify(userRepository).findById(USER_ID);
    }

    @Test
    @DisplayName("Não deve renovar token inválido")
    void naoDeveRenovarTokenInvalido() {
        when(jwtUtil.validateRefreshToken(REFRESH_TOKEN))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.refreshToken(REFRESH_TOKEN)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Não deve aceitar access token como refresh token")
    void naoDeveAceitarAccessTokenComoRefreshToken() {
        when(jwtUtil.validateRefreshToken(ACCESS_TOKEN))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.refreshToken(ACCESS_TOKEN)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Não deve renovar token de conta inativa")
    void naoDeveRenovarTokenContaInativa() {
        User user = buildUser(false);

        when(jwtUtil.validateRefreshToken(REFRESH_TOKEN))
                .thenReturn(true);

        when(jwtUtil.extractUserId(REFRESH_TOKEN))
                .thenReturn(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.refreshToken(REFRESH_TOKEN)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verify(jwtUtil, never())
                .generateAccessToken(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve renovar token de usuário inexistente")
    void naoDeveRenovarTokenUsuarioInexistente() {
        when(jwtUtil.validateRefreshToken(REFRESH_TOKEN))
                .thenReturn(true);

        when(jwtUtil.extractUserId(REFRESH_TOKEN))
                .thenReturn(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.refreshToken(REFRESH_TOKEN)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }

    @Test
    @DisplayName("Não deve renovar token vazio")
    void naoDeveRenovarTokenVazio() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.refreshToken(" ")
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verifyNoInteractions(jwtUtil, userRepository);
    }

    @Test
    @DisplayName("JWT real deve diferenciar access e refresh token")
    void deveDiferenciarTiposDeToken() {
        JwtUtil realJwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(
                realJwtUtil,
                "secret",
                "chave-de-teste-segura-com-mais-de-32-bytes"
        );

        ReflectionTestUtils.setField(
                realJwtUtil,
                "expiration",
                900000L
        );

        ReflectionTestUtils.setField(
                realJwtUtil,
                "refreshExpiration",
                604800000L
        );

        String accessToken = realJwtUtil.generateAccessToken(
                USER_ID, EMAIL, "ATHLETE"
        );

        String refreshToken = realJwtUtil.generateRefreshToken(
                USER_ID, EMAIL, "ATHLETE"
        );

        assertTrue(realJwtUtil.validateAccessToken(accessToken));
        assertFalse(realJwtUtil.validateRefreshToken(accessToken));

        assertTrue(realJwtUtil.validateRefreshToken(refreshToken));
        assertFalse(realJwtUtil.validateAccessToken(refreshToken));
    }

    @Test
    @DisplayName("JWT real deve rejeitar token adulterado")
    void deveRejeitarTokenAdulterado() {
        JwtUtil realJwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(
                realJwtUtil,
                "secret",
                "chave-de-teste-segura-com-mais-de-32-bytes"
        );

        ReflectionTestUtils.setField(
                realJwtUtil,
                "expiration",
                900000L
        );

        String token = realJwtUtil.generateAccessToken(
                USER_ID, EMAIL, "ATHLETE"
        );

        String tokenAdulterado = token + "adulterado";

        assertFalse(
                realJwtUtil.validateAccessToken(tokenAdulterado)
        );
    }
}
