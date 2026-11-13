
package br.com.nextagon.service;

import br.com.nextagon.dto.request.TrainingPlanRequestDto;
import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.TrainingPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceTest {

    @Mock
    private TrainingPlanRepository trainingPlanRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private TrainingPlanService trainingPlanService;

    private static final String CONTRACT_ID = "contract-1";
    private static final String PLAN_ID = "plan-1";
    private static final String PROFESSIONAL_ID = "professional-1";
    private static final String ATHLETE_ID = "athlete-1";

    private Contract contract;

    @BeforeEach
    void setup() {
        User professional = new User();
        professional.setId(PROFESSIONAL_ID);
        professional.setRole(Role.PROFESSIONAL);
        professional.setActive(true);

        User athlete = new User();
        athlete.setId(ATHLETE_ID);
        athlete.setRole(Role.ATHLETE);
        athlete.setActive(true);

        contract = Contract.builder()
                .id(CONTRACT_ID)
                .professional(professional)
                .athlete(athlete)
                .status(ContractStatus.ACTIVE)
                .build();
    }

    private TrainingPlanRequestDto validDto() {
        TrainingPlanRequestDto dto = new TrainingPlanRequestDto();

        ReflectionTestUtils.setField(dto, "contractId", CONTRACT_ID);
        ReflectionTestUtils.setField(dto, "title", "Treino ABC");
        ReflectionTestUtils.setField(
                dto,
                "description",
                "Treino de força"
        );

        return dto;
    }

    private TrainingPlan existingPlan(boolean active) {
        return TrainingPlan.builder()
                .id(PLAN_ID)
                .contract(contract)
                .title("Treino antigo")
                .active(active)
                .build();
    }

    @Test
    void deveCriarPlanoEmContratoAtivo() {
        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(trainingPlanRepository.save(any(TrainingPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TrainingPlan result = trainingPlanService.createPlan(
                PROFESSIONAL_ID,
                validDto()
        );

        assertEquals("Treino ABC", result.getTitle());
        assertEquals(contract, result.getContract());
        assertTrue(result.isActive());

        verify(trainingPlanRepository).save(any(TrainingPlan.class));
    }

    @Test
    void naoDeveCriarPlanoEmContratoPendente() {
        contract.setStatus(ContractStatus.PENDING);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        assertThrows(
                IllegalStateException.class,
                () -> trainingPlanService.createPlan(
                        PROFESSIONAL_ID,
                        validDto()
                )
        );

        verify(trainingPlanRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirOutroProfissionalCriarPlano() {
        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        assertThrows(
                SecurityException.class,
                () -> trainingPlanService.createPlan(
                        "outro-profissional",
                        validDto()
                )
        );

        verify(trainingPlanRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirIntrusoConsultarPlanos() {
        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        assertThrows(
                SecurityException.class,
                () -> trainingPlanService.getPlansByContract(
                        CONTRACT_ID,
                        "usuario-intruso"
                )
        );
    }

    @Test
    void devePermitirAtletaConsultarPlanos() {
        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        trainingPlanService.getPlansByContract(
                CONTRACT_ID,
                ATHLETE_ID
        );

        verify(trainingPlanRepository)
                .findByContractIdAndActiveTrue(CONTRACT_ID);
    }

    @Test
    void naoDevePermitirUsuarioInativoConsultar() {
        contract.getAthlete().setActive(false);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        assertThrows(
                SecurityException.class,
                () -> trainingPlanService.getPlansByContract(
                        CONTRACT_ID,
                        ATHLETE_ID
                )
        );

        verify(trainingPlanRepository, never())
                .findByContractIdAndActiveTrue(any());
    }

    @Test
    void deveAtualizarPlanoAtivo() {
        TrainingPlan plan = existingPlan(true);

        when(trainingPlanRepository.findById(PLAN_ID))
                .thenReturn(Optional.of(plan));

        when(trainingPlanRepository.save(any(TrainingPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TrainingPlan result = trainingPlanService.updatePlan(
                PLAN_ID,
                PROFESSIONAL_ID,
                validDto()
        );

        assertEquals("Treino ABC", result.getTitle());
        assertEquals("Treino de força", result.getDescription());
    }

    @Test
    void naoDeveAtualizarPlanoDesativado() {
        when(trainingPlanRepository.findById(PLAN_ID))
                .thenReturn(Optional.of(existingPlan(false)));

        assertThrows(
                IllegalStateException.class,
                () -> trainingPlanService.updatePlan(
                        PLAN_ID,
                        PROFESSIONAL_ID,
                        validDto()
                )
        );

        verify(trainingPlanRepository, never()).save(any());
    }

    @Test
    void naoDeveAtualizarPlanoComContratoInativo() {
        contract.setStatus(ContractStatus.COMPLETED);

        when(trainingPlanRepository.findById(PLAN_ID))
                .thenReturn(Optional.of(existingPlan(true)));

        assertThrows(
                IllegalStateException.class,
                () -> trainingPlanService.updatePlan(
                        PLAN_ID,
                        PROFESSIONAL_ID,
                        validDto()
                )
        );
    }

    @Test
    void naoDeveTransferirPlanoParaOutroContrato() {
        TrainingPlanRequestDto dto = validDto();

        ReflectionTestUtils.setField(
                dto,
                "contractId",
                "outro-contrato"
        );

        when(trainingPlanRepository.findById(PLAN_ID))
                .thenReturn(Optional.of(existingPlan(true)));

        assertThrows(
                IllegalArgumentException.class,
                () -> trainingPlanService.updatePlan(
                        PLAN_ID,
                        PROFESSIONAL_ID,
                        dto
                )
        );

        verify(trainingPlanRepository, never()).save(any());
    }

    @Test
    void deveDesativarPlano() {
        TrainingPlan plan = existingPlan(true);

        when(trainingPlanRepository.findById(PLAN_ID))
                .thenReturn(Optional.of(plan));

        trainingPlanService.deactivatePlan(
                PLAN_ID,
                PROFESSIONAL_ID
        );

        assertFalse(plan.isActive());
        verify(trainingPlanRepository).save(plan);
    }

    @Test
    void naoDeveDesativarNovamente() {
        TrainingPlan plan = existingPlan(false);

        when(trainingPlanRepository.findById(PLAN_ID))
                .thenReturn(Optional.of(plan));

        trainingPlanService.deactivatePlan(
                PLAN_ID,
                PROFESSIONAL_ID
        );

        verify(trainingPlanRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirAlteracaoComParticipanteInativo() {
        contract.getAthlete().setActive(false);

        when(trainingPlanRepository.findById(PLAN_ID))
                .thenReturn(Optional.of(existingPlan(true)));

        assertThrows(
                SecurityException.class,
                () -> trainingPlanService.updatePlan(
                        PLAN_ID,
                        PROFESSIONAL_ID,
                        validDto()
                )
        );

        verify(trainingPlanRepository, never()).save(any());
    }
}
