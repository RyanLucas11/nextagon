
package br.com.nextagon.service;

import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.MessageRepository;
import br.com.nextagon.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    private static final String CONTRACT_ID = "contract-1";
    private static final String ATHLETE_ID = "athlete-1";
    private static final String PROFESSIONAL_ID = "professional-1";
    private static final String OTHER_ID = "other-1";

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private User buildUser(
            String id,
            Role role,
            boolean active
    ) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setActive(active);
        return user;
    }

    private Contract buildContract(ContractStatus status) {
        Contract contract = new Contract();

        contract.setId(CONTRACT_ID);
        contract.setStatus(status);

        contract.setAthlete(
                buildUser(ATHLETE_ID, Role.ATHLETE, true)
        );

        contract.setProfessional(
                buildUser(PROFESSIONAL_ID, Role.PROFESSIONAL, true)
        );

        return contract;
    }

    private void assertStatus(
            HttpStatus expected,
            ResponseStatusException exception
    ) {
        assertEquals(expected, exception.getStatusCode());
    }

    @Test
    @DisplayName("Atleta pode enviar mensagem em contrato ativo")
    void devePermitirAtletaEnviarMensagem() {
        Contract contract = buildContract(ContractStatus.ACTIVE);
        User athlete = contract.getAthlete();

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(athlete));

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message result = chatService.sendMessage(
                CONTRACT_ID,
                ATHLETE_ID,
                "Olá, profissional!",
                null
        );

        assertNotNull(result);
        assertEquals(ATHLETE_ID, result.getSender().getId());
        assertEquals(PROFESSIONAL_ID, result.getReceiver().getId());
        assertEquals("Olá, profissional!", result.getContent());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("Profissional pode responder ao atleta")
    void devePermitirProfissionalEnviarMensagem() {
        Contract contract = buildContract(ContractStatus.ACTIVE);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.of(contract.getProfessional()));

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message result = chatService.sendMessage(
                CONTRACT_ID,
                PROFESSIONAL_ID,
                "Olá, atleta!",
                null
        );

        assertEquals(PROFESSIONAL_ID, result.getSender().getId());
        assertEquals(ATHLETE_ID, result.getReceiver().getId());
    }

    @Test
    @DisplayName("Terceiros não podem enviar mensagens")
    void naoDevePermitirTerceiroEnviarMensagem() {
        Contract contract = buildContract(ContractStatus.ACTIVE);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.sendMessage(
                        CONTRACT_ID,
                        OTHER_ID,
                        "Mensagem indevida",
                        null
                )
        );

        assertStatus(HttpStatus.FORBIDDEN, exception);

        verifyNoInteractions(messageRepository, userRepository);
    }

    @Test
    @DisplayName("Usuário inativo não pode enviar mensagens")
    void naoDevePermitirUsuarioInativoEnviarMensagem() {
        Contract contract = buildContract(ContractStatus.ACTIVE);

        User inactiveAthlete = buildUser(
                ATHLETE_ID,
                Role.ATHLETE,
                false
        );

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(inactiveAthlete));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.sendMessage(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        "Mensagem",
                        null
                )
        );

        assertStatus(HttpStatus.FORBIDDEN, exception);

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve enviar mensagens em contrato pendente")
    void naoDeveEnviarMensagemContratoPendente() {
        Contract contract = buildContract(ContractStatus.PENDING);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(contract.getAthlete()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.sendMessage(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        "Mensagem",
                        null
                )
        );

        assertStatus(HttpStatus.CONFLICT, exception);

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve enviar mensagem sem texto e sem anexo")
    void naoDeveEnviarMensagemVazia() {
        Contract contract = buildContract(ContractStatus.ACTIVE);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(contract.getAthlete()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.sendMessage(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        " ",
                        null
                )
        );

        assertStatus(HttpStatus.BAD_REQUEST, exception);

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Participante ativo pode consultar histórico")
    void devePermitirConsultarHistorico() {
        Contract contract = buildContract(ContractStatus.ACTIVE);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(contract.getAthlete()));

        when(messageRepository.findByContractIdOrderByCreatedAtAsc(CONTRACT_ID))
                .thenReturn(List.of());

        List<Message> result = chatService.getMessagesByContract(
                CONTRACT_ID,
                ATHLETE_ID
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(messageRepository)
                .findByContractIdOrderByCreatedAtAsc(CONTRACT_ID);
    }

    @Test
    @DisplayName("Terceiros não podem consultar histórico")
    void naoDevePermitirTerceiroConsultarHistorico() {
        Contract contract = buildContract(ContractStatus.ACTIVE);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.getMessagesByContract(
                        CONTRACT_ID,
                        OTHER_ID
                )
        );

        assertStatus(HttpStatus.FORBIDDEN, exception);

        verifyNoInteractions(messageRepository);
    }

    @Test
    @DisplayName("Usuário inativo não pode consultar histórico")
    void naoDevePermitirInativoConsultarHistorico() {
        Contract contract = buildContract(ContractStatus.ACTIVE);

        User inactiveAthlete = buildUser(
                ATHLETE_ID,
                Role.ATHLETE,
                false
        );

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(inactiveAthlete));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.getMessagesByContract(
                        CONTRACT_ID,
                        ATHLETE_ID
                )
        );

        assertStatus(HttpStatus.FORBIDDEN, exception);

        verifyNoInteractions(messageRepository);
    }

    @Test
    @DisplayName("Contrato inexistente deve retornar 404")
    void deveRejeitarContratoInexistente() {
        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.getMessagesByContract(
                        CONTRACT_ID,
                        ATHLETE_ID
                )
        );

        assertStatus(HttpStatus.NOT_FOUND, exception);

        verifyNoInteractions(messageRepository);
    }
}
