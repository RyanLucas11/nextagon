package br.com.nextagon.service;

import br.com.nextagon.dto.request.DietPlanRequestDto;
import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.DietPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietPlanServiceTest {

    @Mock
    private DietPlanRepository dietPlanRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private DietPlanService dietPlanService;

    private static final String CONTRACT_ID = "contract-1";
    private static final String PLAN_ID = "plan-1";
    private static final String ATHLETE_ID = "athlete-1";
    private static final String PROFESSIONAL_ID = "prof-1";
    private static final String OUTRO_PROFESSIONAL_ID = "prof-2";

    private User buildAthlete(String id) {
        User athlete = new User();
        athlete.setId(id);
        athlete.setRole(Role.ATHLETE);
        return athlete;
    }

    private User buildProfessional(String id) {
        User professional = new User();
        professional.setId(id);
        professional.setRole(Role.PROFESSIONAL);
        return professional;
    }

    private Contract buildContract(ContractStatus status) {
        return Contract.builder()
                .id(CONTRACT_ID)
                .athlete(buildAthlete(ATHLETE_ID))
                .professional(buildProfessional(PROFESSIONAL_ID))
                .status(status)
                .build();
    }

    private DietPlanRequestDto buildDto() {
        DietPlanRequestDto dto = new DietPlanRequestDto();
        setField(dto, "contractId", CONTRACT_ID);
        setField(dto, "title", "Dieta de cutting - fase 1");
        setField(dto, "totalCalories", 2200);
        setField(dto, "proteinG", 180.0);
        setField(dto, "carbsG", 220.0);
        setField(dto, "fatsG", 60.0);
        setField(dto, "notes", "Evitar carboidrato simples à noite");
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
    // 1. SUCESSO — criar plano
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve criar um plano de dieta para um contrato ACTIVE do profissional correto")
    void deveCriarPlanoComSucesso() {
        Contract contratoAtivo = buildContract(ContractStatus.ACTIVE);
        DietPlanRequestDto dto = buildDto();

        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contratoAtivo));
        when(dietPlanRepository.save(any(DietPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DietPlan resultado = dietPlanService.createPlan(PROFESSIONAL_ID, dto);

        assertEquals("Dieta de cutting - fase 1", resultado.getTitle());
        verify(dietPlanRepository, times(1)).save(any(DietPlan.class));
    }

    // ---------------------------------------------------------------
    // 2. NÃO ENCONTRADO — contrato inexistente
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Não deve criar plano quando o contrato não existe")
    void naoDeveCriarPlanoContratoInexistente() {
        DietPlanRequestDto dto = buildDto();
        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dietPlanService.createPlan(PROFESSIONAL_ID, dto)
        );

        assertEquals("Contrato não encontrado", exception.getMessage());
        verify(dietPlanRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 3. DADOS INVÁLIDOS — contrato não está ACTIVE
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Não deve criar plano para contrato que não está ACTIVE")
    void naoDeveCriarPlanoContratoNaoAtivo() {
        Contract contratoPendente = buildContract(ContractStatus.PENDING);
        DietPlanRequestDto dto = buildDto();

        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contratoPendente));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dietPlanService.createPlan(PROFESSIONAL_ID, dto)
        );

        assertEquals("Contrato não está ACTIVE", exception.getMessage());
        verify(dietPlanRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 4. OPERAÇÃO INADEQUADA — profissional não é dono do contrato
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Não deve permitir que outro profissional crie plano no contrato")
    void naoDevePermitirProfissionalErradoCriarPlano() {
        Contract contratoAtivo = buildContract(ContractStatus.ACTIVE);
        DietPlanRequestDto dto = buildDto();

        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contratoAtivo));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> dietPlanService.createPlan(OUTRO_PROFESSIONAL_ID, dto)
        );

        assertEquals("Acesso negado: você não é o profissional deste contrato", exception.getMessage());
        verify(dietPlanRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 5. OPERAÇÃO INADEQUADA — buscar planos sem ser participante
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Não deve permitir que um usuário fora do contrato veja os planos")
    void naoDevePermitirNaoParticipanteVerPlanos() {
        Contract contratoAtivo = buildContract(ContractStatus.ACTIVE);
        String intrusoId = "user-estranho";

        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contratoAtivo));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> dietPlanService.getPlansByContract(CONTRACT_ID, intrusoId)
        );

        assertEquals("Acesso negado: você não é participante deste contrato", exception.getMessage());
        verify(dietPlanRepository, never()).findByContractIdAndActiveTrue(any());
    }

    // ---------------------------------------------------------------
    // 6. NÃO ENCONTRADO — atualizar plano inexistente
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Não deve atualizar um plano de dieta que não existe")
    void naoDeveAtualizarPlanoInexistente() {
        DietPlanRequestDto dto = buildDto();
        when(dietPlanRepository.findById(PLAN_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dietPlanService.updatePlan(PLAN_ID, PROFESSIONAL_ID, dto)
        );

        assertEquals("Plano de dieta não encontrado", exception.getMessage());
        verify(dietPlanRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 7. VERIFY — atualizar plano com sucesso
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve atualizar os dados nutricionais do plano corretamente")
    void deveAtualizarPlanoComSucesso() {
        Contract contratoAtivo = buildContract(ContractStatus.ACTIVE);
        DietPlan planoExistente = DietPlan.builder()
                .id(PLAN_ID)
                .contract(contratoAtivo)
                .title("Título antigo")
                .totalCalories(2000)
                .proteinG(150.0)
                .carbsG(200.0)
                .fatsG(50.0)
                .build();

        DietPlanRequestDto dto = buildDto();
        ArgumentCaptor<DietPlan> captor = ArgumentCaptor.forClass(DietPlan.class);

        when(dietPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(planoExistente));
        when(dietPlanRepository.save(any(DietPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        dietPlanService.updatePlan(PLAN_ID, PROFESSIONAL_ID, dto);

        verify(dietPlanRepository).save(captor.capture());
        assertEquals("Dieta de cutting - fase 1", captor.getValue().getTitle());
        assertEquals(2200, captor.getValue().getTotalCalories());
        assertEquals(180.0, captor.getValue().getProteinG());
    }

    // ---------------------------------------------------------------
    // 8. VERIFY — desativar plano
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Deve desativar o plano de dieta definindo active como false")
    void deveDesativarPlanoComSucesso() {
        Contract contratoAtivo = buildContract(ContractStatus.ACTIVE);
        DietPlan planoExistente = DietPlan.builder()
                .id(PLAN_ID)
                .contract(contratoAtivo)
                .active(true)
                .build();

        ArgumentCaptor<DietPlan> captor = ArgumentCaptor.forClass(DietPlan.class);

        when(dietPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(planoExistente));
        when(dietPlanRepository.save(any(DietPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        dietPlanService.deactivatePlan(PLAN_ID, PROFESSIONAL_ID);

        verify(dietPlanRepository).save(captor.capture());
        assertFalse(captor.getValue().isActive());
    }
}