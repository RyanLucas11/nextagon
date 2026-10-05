package br.com.nextagon.service;

import br.com.nextagon.dto.request.AthleteProfileRequestDto;
import br.com.nextagon.dto.request.ProfessionalProfileRequestDto;
import br.com.nextagon.model.AthleteProfile;
import br.com.nextagon.model.ProfessionalProfile;
import br.com.nextagon.model.Role;
import br.com.nextagon.model.User;
import br.com.nextagon.repository.ProfessionalProfileRepository;
import br.com.nextagon.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfessionalProfileRepository professionalProfileRepository;

    @InjectMocks
    private ProfileService profileService;

    private static final String USER_ID = "user-1";

    private User buildAthleteUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setRole(Role.ATHLETE);
        return user;
    }

    private User buildProfessionalUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setRole(Role.PROFESSIONAL);
        return user;
    }

    private AthleteProfileRequestDto buildAthleteDto() {
        AthleteProfileRequestDto dto = new AthleteProfileRequestDto();
        setField(dto, "height", 1.80);
        setField(dto, "weight", 80.0);
        setField(dto, "healthNotes", "Sem restrições");
        setField(dto, "fitnessLevel", "INTERMEDIARIO");
        setField(dto, "goals", Arrays.asList("Hipertrofia", "Resistência"));
        setField(dto, "displayName", "Ryan Atleta");
        return dto;
    }

    private ProfessionalProfileRequestDto buildProfessionalDto() {
        ProfessionalProfileRequestDto dto = new ProfessionalProfileRequestDto();
        setField(dto, "bio", "Personal trainer há 10 anos");
        setField(dto, "hourlyRate", 150.0);
        setField(dto, "available", true);
        setField(dto, "specialties", Arrays.asList("Musculação", "Funcional"));
        setField(dto, "certificates", Arrays.asList("CREF 12345"));
        setField(dto, "displayName", "Ryan Personal");
        return dto;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao setar campo via reflection: " + fieldName, e);
        }
    }

    // ---------------------------------------------------------------
    // 1. SUCESSO — criar perfil de atleta novo
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve criar um novo perfil de atleta quando o usuário ainda não tem um")
    void deveCriarPerfilAtletaNovo() {
        User athlete = buildAthleteUser();
        AthleteProfileRequestDto dto = buildAthleteDto();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(athlete));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AthleteProfile resultado = profileService.saveAthleteProfile(USER_ID, dto);

        assertEquals(1.80, resultado.getHeight());
        assertEquals(80.0, resultado.getWeight());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ---------------------------------------------------------------
    // 2. NÃO ENCONTRADO — usuário inexistente
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Não deve salvar perfil de atleta quando o usuário não existe")
    void naoDeveSalvarPerfilAtletaUsuarioInexistente() {
        AthleteProfileRequestDto dto = buildAthleteDto();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> profileService.saveAthleteProfile(USER_ID, dto)
        );

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 3. OPERAÇÃO INADEQUADA — role errada (não é ATHLETE)
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Não deve permitir que um PROFESSIONAL crie perfil de atleta")
    void naoDevePermitirProfissionalCriarPerfilAtleta() {
        User professional = buildProfessionalUser();
        AthleteProfileRequestDto dto = buildAthleteDto();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(professional));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> profileService.saveAthleteProfile(USER_ID, dto)
        );

        assertEquals("Apenas atletas podem criar perfil de atleta", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 4. VERIFY — atualizar perfil de atleta existente
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve atualizar um perfil de atleta já existente com os novos dados")
    void deveAtualizarPerfilAtletaExistente() {
        User athlete = buildAthleteUser();
        AthleteProfile perfilExistente = AthleteProfile.builder()
                .user(athlete)
                .height(1.70)
                .weight(70.0)
                .build();
        athlete.setAthleteProfile(perfilExistente);

        AthleteProfileRequestDto dto = buildAthleteDto();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(athlete));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AthleteProfile resultado = profileService.saveAthleteProfile(USER_ID, dto);

        assertEquals(1.80, resultado.getHeight());
        assertEquals(80.0, resultado.getWeight());
        assertEquals("Sem restrições", resultado.getHealthNotes());
    }

    // ---------------------------------------------------------------
    // 5. NÃO ENCONTRADO — buscar perfil de atleta inexistente
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve lançar exceção ao buscar perfil de atleta que não existe")
    void deveLancarExcecaoAoBuscarPerfilAtletaInexistente() {
        User athleteSemPerfil = buildAthleteUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(athleteSemPerfil));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> profileService.getAthleteProfile(USER_ID)
        );

        assertEquals("Perfil de atleta não encontrado", exception.getMessage());
    }

    // ---------------------------------------------------------------
    // 6. SUCESSO — criar perfil profissional novo
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve criar um novo perfil profissional quando o usuário ainda não tem um")
    void deveCriarPerfilProfissionalNovo() {
        User professional = buildProfessionalUser();
        ProfessionalProfileRequestDto dto = buildProfessionalDto();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(professional));
        when(professionalProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(professionalProfileRepository.save(any(ProfessionalProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProfessionalProfile resultado = profileService.saveProfessionalProfile(USER_ID, dto);

        assertEquals("Personal trainer há 10 anos", resultado.getBio());
        assertEquals(150.0, resultado.getHourlyRate());
        verify(professionalProfileRepository, times(1)).save(any(ProfessionalProfile.class));
    }

    // ---------------------------------------------------------------
    // 7. OPERAÇÃO INADEQUADA — role errada (não é PROFESSIONAL)
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Não deve permitir que um ATHLETE crie perfil profissional")
    void naoDevePermitirAtletaCriarPerfilProfissional() {
        User athlete = buildAthleteUser();
        ProfessionalProfileRequestDto dto = buildProfessionalDto();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(athlete));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> profileService.saveProfessionalProfile(USER_ID, dto)
        );

        assertEquals("Apenas profissionais podem criar perfil profissional", exception.getMessage());
        verify(professionalProfileRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 8. NÃO ENCONTRADO — buscar perfil profissional inexistente
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve lançar exceção ao buscar perfil profissional que não existe")
    void deveLancarExcecaoAoBuscarPerfilProfissionalInexistente() {
        when(professionalProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> profileService.getProfessionalProfile(USER_ID)
        );

        assertEquals("Perfil profissional não encontrado", exception.getMessage());
    }
}