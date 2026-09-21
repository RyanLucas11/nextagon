package br.com.nextagon.service;

import br.com.nextagon.dto.AuthResponseDto;
import br.com.nextagon.dto.LoginDto;
import br.com.nextagon.dto.RegisterDto;
import br.com.nextagon.model.Role;
import br.com.nextagon.model.User;
import br.com.nextagon.repository.UserRepository;
import br.com.nextagon.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private static final String EMAIL = "atleta@nextagon.com";
    private static final String RAW_PASSWORD = "senha123";
    private static final String ENCODED_PASSWORD = "$2a$senha-criptografada";
    private static final String USER_ID = "user-1";

    private RegisterDto buildRegisterDto(Role role) {
        RegisterDto dto = new RegisterDto();
        dto.setName("Ryan Atleta");
        dto.setEmail(EMAIL);
        dto.setPassword(RAW_PASSWORD);
        dto.setRole(role);
        return dto;
    }

    private LoginDto buildLoginDto() {
        LoginDto dto = new LoginDto();
        dto.setEmail(EMAIL);
        dto.setPassword(RAW_PASSWORD);
        return dto;
    }

    private User buildUser(boolean active) {
        User user = User.builder()
                .id(USER_ID)
                .name("Ryan Atleta")
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.ATHLETE)
                .build();
        user.setActive(active);
        return user;
    }

    // ---------------------------------------------------------------
    // 1. SUCESSO — registro
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve registrar um novo usuário ATHLETE com sucesso")
    void deveRegistrarUsuarioComSucesso() {
        RegisterDto dto = buildRegisterDto(Role.ATHLETE);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any(), any(), any())).thenReturn("refresh-token");

        AuthResponseDto resultado = authService.register(dto);

        assertNotNull(resultado);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(RAW_PASSWORD);
    }

    // ---------------------------------------------------------------
    // 2. DADOS INVÁLIDOS — email já cadastrado
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Não deve permitir registrar com email já cadastrado")
    void naoDevePermitirEmailDuplicado() {
        RegisterDto dto = buildRegisterDto(Role.ATHLETE);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser(true)));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(dto)
        );

        assertEquals("Email já cadastrado", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 3. OPERAÇÃO INADEQUADA — tentativa de criar ADMIN
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Não deve permitir criar conta ADMIN pelo endpoint público")
    void naoDevePermitirCriarContaAdmin() {
        RegisterDto dto = buildRegisterDto(Role.ADMIN);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> authService.register(dto)
        );

        assertEquals("Não é permitido criar uma conta de administrador por este endpoint", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 4. VERIFY — senha é criptografada antes de salvar
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve salvar o usuário com a senha criptografada, nunca em texto puro")
    void deveSalvarUsuarioComSenhaCriptografada() {
        RegisterDto dto = buildRegisterDto(Role.PROFESSIONAL);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any(), any(), any())).thenReturn("refresh-token");

        authService.register(dto);

        verify(userRepository).save(captor.capture());
        User usuarioSalvo = captor.getValue();
        assertEquals(ENCODED_PASSWORD, usuarioSalvo.getPassword());
        assertNotEquals(RAW_PASSWORD, usuarioSalvo.getPassword());
    }

    // ---------------------------------------------------------------
    // 5. SUCESSO — login
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve autenticar um usuário ativo com credenciais corretas")
    void deveAutenticarComSucesso() {
        LoginDto dto = buildLoginDto();
        User usuarioAtivo = buildUser(true);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioAtivo));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any(), any(), any())).thenReturn("refresh-token");

        AuthResponseDto resultado = authService.login(dto);

        assertNotNull(resultado);
        verify(jwtUtil, times(1)).generateAccessToken(USER_ID, EMAIL, Role.ATHLETE.name());
    }

    // ---------------------------------------------------------------
    // 6. NÃO ENCONTRADO — email não cadastrado
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve rejeitar login com email não cadastrado")
    void deveRejeitarLoginComEmailInexistente() {
        LoginDto dto = buildLoginDto();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(dto)
        );

        assertEquals("Credenciais inválidas", exception.getMessage());
    }

    // ---------------------------------------------------------------
    // 7. FALHA DE REGRA DE NEGÓCIO — conta inativa
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Não deve permitir login em conta inativa")
    void naoDevePermitirLoginContaInativa() {
        LoginDto dto = buildLoginDto();
        User usuarioInativo = buildUser(false);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioInativo));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.login(dto)
        );

        assertEquals("Conta inativa", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    // ---------------------------------------------------------------
    // 8. DADOS INVÁLIDOS — senha incorreta
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve rejeitar login com senha incorreta")
    void deveRejeitarLoginComSenhaIncorreta() {
        LoginDto dto = buildLoginDto();
        User usuarioAtivo = buildUser(true);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioAtivo));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(dto)
        );

        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(jwtUtil, never()).generateAccessToken(any(), any(), any());
    }
}