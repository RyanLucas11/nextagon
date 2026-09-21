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

import java.util.ArrayList;
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

    private static final String ATHLETE_ID = "athlete-1";
    private static final String PROFESSIONAL_ID = "prof-1";
    private static final String USER_ID = "user-1";

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private User buildAthlete(String id) {
        User user = new User();
        user.setId(id);
        user.setRole(Role.ATHLETE);
        return user;
    }

    private User buildProfessional(String id) {
        User user = new User();
        user.setId(id);
        user.setRole(Role.PROFESSIONAL);
        return user;
    }

    private AthleteProfileRequestDto buildAthleteDto() {
        AthleteProfileRequestDto dto = new AthleteProfileRequestDto();

        setField(dto, "displayName", "Ryan Atleta");
        setField(dto, "height", 180.0);
        setField(dto, "weight", 75.0);
        setField(dto, "goals", Arrays.asList("Hipertrofia", "Condicionamento"));
        setField(dto, "healthNotes", "Sem restrições");
        setField(dto, "fitnessLevel", "intermediate");

        return dto;
    }

    private ProfessionalProfileRequestDto buildProfessionalDto() {
        ProfessionalProfileRequestDto dto = new ProfessionalProfileRequestDto();

        setField(dto, "displayName", "Profissional Teste");
        setField(dto, "bio", "Personal trainer especializado em musculação");
        setField(dto, "specialties",
                Arrays.asList("Musculação", "Hipertrofia"));
        setField(dto, "certificates",
                Arrays.asList("CREF", "Certificação de Personal"));
        setField(dto, "hourlyRate", 100.0);
        setField(dto, "available", true);

        return dto;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field =
                    target.getClass().getDeclaredField(fieldName);

            field.setAccessible(true);
            field.set(target, value);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao setar campo via reflection: " + fieldName, e
            );
        }
    }

    // ===============================================================
    // ATHLETE
    // ===============================================================

    // ---------------------------------------------------------------
    // 1. Criar perfil de atleta
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve criar perfil de atleta com sucesso")
    void deveCriarPerfilAtletaComSucesso() {

        User user = buildAthlete(ATHLETE_ID);
        AthleteProfileRequestDto dto = buildAthleteDto();

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AthleteProfile resultado =
                profileService.saveAthleteProfile(ATHLETE_ID, dto);

        assertNotNull(resultado);
        assertSame(user, resultado.getUser());

        assertEquals(180.0, resultado.getHeight());
        assertEquals(75.0, resultado.getWeight());
        assertEquals("Sem restrições", resultado.getHealthNotes());
        assertEquals("intermediate", resultado.getFitnessLevel());

        assertEquals(
                Arrays.asList("Hipertrofia", "Condicionamento"),
                resultado.getGoals()
        );

        assertEquals("Ryan Atleta", user.getName());

        verify(userRepository).save(user);
    }

    // ---------------------------------------------------------------
    // 2. Usuário inexistente
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Não deve criar perfil quando o usuário não existe")
    void naoDeveCriarPerfilAtletaUsuarioInexistente() {

        AthleteProfileRequestDto dto = buildAthleteDto();

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> profileService.saveAthleteProfile(ATHLETE_ID, dto)
        );

        assertEquals(
                "Usuário não encontrado",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 3. Usuário não é atleta
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Não deve permitir que profissional crie perfil de atleta")
    void naoDevePermitirProfissionalCriarPerfilAtleta() {

        User user = buildProfessional(PROFESSIONAL_ID);
        AthleteProfileRequestDto dto = buildAthleteDto();

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.of(user));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> profileService.saveAthleteProfile(
                        PROFESSIONAL_ID,
                        dto
                )
        );

        assertEquals(
                "Apenas atletas podem criar perfil de atleta",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 4. Atualizar perfil de atleta existente
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve atualizar perfil de atleta existente")
    void deveAtualizarPerfilAtletaExistente() {

        User user = buildAthlete(ATHLETE_ID);

        AthleteProfile profile = AthleteProfile.builder()
                .user(user)
                .height(170.0)
                .weight(70.0)
                .goals(new ArrayList<>(
                        Arrays.asList("Emagrecimento")
                ))
                .healthNotes("Observação antiga")
                .fitnessLevel("beginner")
                .build();

        user.setAthleteProfile(profile);

        AthleteProfileRequestDto dto = buildAthleteDto();

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AthleteProfile resultado =
                profileService.saveAthleteProfile(ATHLETE_ID, dto);

        assertSame(profile, resultado);

        assertEquals(180.0, resultado.getHeight());
        assertEquals(75.0, resultado.getWeight());
        assertEquals("Sem restrições", resultado.getHealthNotes());
        assertEquals("intermediate", resultado.getFitnessLevel());

        assertEquals(
                Arrays.asList("Hipertrofia", "Condicionamento"),
                resultado.getGoals()
        );

        assertEquals("Ryan Atleta", user.getName());

        verify(userRepository).save(user);
    }

    // ---------------------------------------------------------------
    // 5. Buscar perfil de atleta
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve buscar perfil de atleta existente")
    void deveBuscarPerfilAtleta() {

        User user = buildAthlete(ATHLETE_ID);

        AthleteProfile profile = AthleteProfile.builder()
                .user(user)
                .height(180.0)
                .weight(75.0)
                .build();

        user.setAthleteProfile(profile);

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(user));

        AthleteProfile resultado =
                profileService.getAthleteProfile(ATHLETE_ID);

        assertSame(profile, resultado);
    }

    // ---------------------------------------------------------------
    // 6. Perfil de atleta inexistente
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve informar quando perfil de atleta não existe")
    void deveFalharAoBuscarPerfilAtletaInexistente() {

        User user = buildAthlete(ATHLETE_ID);
        user.setAthleteProfile(null);

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(user));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> profileService.getAthleteProfile(ATHLETE_ID)
        );

        assertEquals(
                "Perfil de atleta não encontrado",
                exception.getMessage()
        );
    }

    // ===============================================================
    // PROFESSIONAL
    // ===============================================================

    // ---------------------------------------------------------------
    // 7. Criar perfil profissional
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve criar perfil profissional com sucesso")
    void deveCriarPerfilProfissionalComSucesso() {

        User user = buildProfessional(PROFESSIONAL_ID);
        ProfessionalProfileRequestDto dto = buildProfessionalDto();

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.of(user));

        when(professionalProfileRepository.findByUserId(PROFESSIONAL_ID))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(professionalProfileRepository.save(any(ProfessionalProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProfessionalProfile resultado =
                profileService.saveProfessionalProfile(
                        PROFESSIONAL_ID,
                        dto
                );

        assertNotNull(resultado);
        assertSame(user, resultado.getUser());

        assertEquals(
                "Personal trainer especializado em musculação",
                resultado.getBio()
        );

        assertEquals(
                Arrays.asList("Musculação", "Hipertrofia"),
                resultado.getSpecialties()
        );

        assertEquals(
                Arrays.asList("CREF", "Certificação de Personal"),
                resultado.getCertificates()
        );

        assertEquals(100.0, resultado.getHourlyRate());
        assertTrue(resultado.isAvailable());
        assertEquals("Profissional Teste", user.getName());

        verify(userRepository).save(user);
        verify(professionalProfileRepository).save(any(ProfessionalProfile.class));
    }

    // ---------------------------------------------------------------
    // 8. Usuário inexistente
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Não deve criar perfil profissional quando usuário não existe")
    void naoDeveCriarPerfilProfissionalUsuarioInexistente() {

        ProfessionalProfileRequestDto dto = buildProfessionalDto();

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> profileService.saveProfessionalProfile(
                        PROFESSIONAL_ID,
                        dto
                )
        );

        assertEquals(
                "Usuário não encontrado",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
        verify(professionalProfileRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 9. Usuário não é profissional
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Não deve permitir atleta criar perfil profissional")
    void naoDevePermitirAtletaCriarPerfilProfissional() {

        User user = buildAthlete(ATHLETE_ID);
        ProfessionalProfileRequestDto dto = buildProfessionalDto();

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(user));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> profileService.saveProfessionalProfile(
                        ATHLETE_ID,
                        dto
                )
        );

        assertEquals(
                "Apenas profissionais podem criar perfil profissional",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
        verify(professionalProfileRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 10. Atualizar perfil profissional existente
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve atualizar perfil profissional existente")
    void deveAtualizarPerfilProfissionalExistente() {

        User user = buildProfessional(PROFESSIONAL_ID);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user)
                .bio("Bio antiga")
                .specialties(new ArrayList<>(
                        Arrays.asList("Cardio")
                ))
                .certificates(new ArrayList<>(
                        Arrays.asList("Certificado antigo")
                ))
                .hourlyRate(50.0)
                .available(false)
                .build();

        ProfessionalProfileRequestDto dto = buildProfessionalDto();

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.of(user));

        when(professionalProfileRepository.findByUserId(PROFESSIONAL_ID))
                .thenReturn(Optional.of(profile));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(professionalProfileRepository.save(any(ProfessionalProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProfessionalProfile resultado =
                profileService.saveProfessionalProfile(
                        PROFESSIONAL_ID,
                        dto
                );

        assertSame(profile, resultado);

        assertEquals(
                "Personal trainer especializado em musculação",
                resultado.getBio()
        );

        assertEquals(
                Arrays.asList("Musculação", "Hipertrofia"),
                resultado.getSpecialties()
        );

        assertEquals(
                Arrays.asList("CREF", "Certificação de Personal"),
                resultado.getCertificates()
        );

        assertEquals(100.0, resultado.getHourlyRate());
        assertTrue(resultado.isAvailable());
        assertEquals("Profissional Teste", user.getName());

        verify(professionalProfileRepository)
                .save(profile);
    }

    // ---------------------------------------------------------------
    // 11. Buscar perfil profissional
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve buscar perfil profissional existente")
    void deveBuscarPerfilProfissional() {

        User user = buildProfessional(PROFESSIONAL_ID);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user)
                .bio("Personal trainer")
                .hourlyRate(100.0)
                .available(true)
                .build();

        when(professionalProfileRepository.findByUserId(PROFESSIONAL_ID))
                .thenReturn(Optional.of(profile));

        ProfessionalProfile resultado =
                profileService.getProfessionalProfile(PROFESSIONAL_ID);

        assertSame(profile, resultado);
    }

    // ---------------------------------------------------------------
    // 12. Perfil profissional inexistente
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve informar quando perfil profissional não existe")
    void deveFalharAoBuscarPerfilProfissionalInexistente() {

        when(professionalProfileRepository.findByUserId(PROFESSIONAL_ID))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> profileService.getProfessionalProfile(PROFESSIONAL_ID)
        );

        assertEquals(
                "Perfil profissional não encontrado",
                exception.getMessage()
        );
    }
}