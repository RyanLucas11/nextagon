package br.com.nextagon.service;

import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.MessageRepository;
import br.com.nextagon.repository.UserRepository;
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
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ContractService contractService;

    private static final String CONTRACT_ID = "contract-1";
    private static final String PROFESSIONAL_ID = "prof-1";
    private static final String ATHLETE_ID = "athlete-1";
    private static final String OUTRO_PROFESSIONAL_ID = "prof-2";

    private User buildProfessional(String id) {
        User professional = new User();
        professional.setId(id);
        professional.setRole(Role.PROFESSIONAL);
        return professional;
    }

    private User buildAthlete(String id) {
        User athlete = new User();
        athlete.setId(id);
        athlete.setRole(Role.ATHLETE);
        return athlete;
    }

    private Contract buildContract(ContractStatus status, String professionalId) {
        return Contract.builder()
                .id(CONTRACT_ID)
                .athlete(buildAthlete(ATHLETE_ID))
                .professional(buildProfessional(professionalId))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("Deve concluir um contrato ACTIVE quando o profissional correto solicita")
    void deveConcluirContratoAtivoComSucesso() {
        Contract contratoAtivo = buildContract(ContractStatus.ACTIVE, PROFESSIONAL_ID);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contratoAtivo));
        when(contractRepository.save(any(Contract.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Contract resultado = contractService.completeContract(CONTRACT_ID, PROFESSIONAL_ID);

        assertEquals(ContractStatus.COMPLETED, resultado.getStatus());
        assertNotNull(resultado.getCompletedAt());
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    @DisplayName("Não deve permitir concluir um contrato que não está ACTIVE")
    void naoDeveConcluirContratoQueNaoEstaAtivo() {
        Contract contratoPendente = buildContract(ContractStatus.PENDING, PROFESSIONAL_ID);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contratoPendente));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> contractService.completeContract(CONTRACT_ID, PROFESSIONAL_ID)
        );

        assertEquals("Apenas contratos ACTIVE podem ser concluídos", exception.getMessage());
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o contrato não existe")
    void deveLancarExcecaoQuandoContratoNaoExiste() {
        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> contractService.completeContract(CONTRACT_ID, PROFESSIONAL_ID)
        );

        assertEquals("Contrato não encontrado", exception.getMessage());
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve salvar o contrato com status COMPLETED e completedAt preenchido")
    void deveSalvarContratoComStatusCompletedCorretamente() {
        Contract contratoAtivo = buildContract(ContractStatus.ACTIVE, PROFESSIONAL_ID);
        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contratoAtivo));
        when(contractRepository.save(any(Contract.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        contractService.completeContract(CONTRACT_ID, PROFESSIONAL_ID);

        verify(contractRepository).save(captor.capture());
        Contract contratoSalvo = captor.getValue();

        assertEquals(ContractStatus.COMPLETED, contratoSalvo.getStatus());
        assertNotNull(contratoSalvo.getCompletedAt());
    }

    @Test
    @DisplayName("Não deve permitir que outro profissional conclua o contrato")
    void naoDevePermitirProfissionalErradoConcluirContrato() {
        Contract contratoAtivo = buildContract(ContractStatus.ACTIVE, PROFESSIONAL_ID);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contratoAtivo));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> contractService.completeContract(CONTRACT_ID, OUTRO_PROFESSIONAL_ID)
        );

        assertEquals("Apenas o profissional pode concluir o contrato", exception.getMessage());
        verify(contractRepository, never()).save(any());
    }
}