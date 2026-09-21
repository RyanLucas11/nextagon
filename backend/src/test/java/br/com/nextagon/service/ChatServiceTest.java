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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private static final String CONTRACT_ID = "contract-1";
    private static final String ATHLETE_ID = "athlete-1";
    private static final String PROFESSIONAL_ID = "prof-1";
    private static final String INTRUDER_ID = "user-estranho";

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private User buildAthlete() {
        User athlete = new User();
        athlete.setId(ATHLETE_ID);
        athlete.setRole(Role.ATHLETE);
        return athlete;
    }

    private User buildProfessional() {
        User professional = new User();
        professional.setId(PROFESSIONAL_ID);
        professional.setRole(Role.PROFESSIONAL);
        return professional;
    }

    private Contract buildContract(ContractStatus status) {
        return Contract.builder()
                .id(CONTRACT_ID)
                .athlete(buildAthlete())
                .professional(buildProfessional())
                .status(status)
                .build();
    }

    // ===============================================================
    // SEND MESSAGE
    // ===============================================================

    // ---------------------------------------------------------------
    // 1. Atleta envia mensagem com sucesso
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve permitir que o atleta envie mensagem no contrato ACTIVE")
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
        assertSame(contract, result.getContract());
        assertSame(athlete, result.getSender());
        assertSame(contract.getProfessional(), result.getReceiver());
        assertEquals("Olá, profissional!", result.getContent());
        assertNull(result.getAttachmentUrl());

        verify(messageRepository).save(any(Message.class));
    }

    // ---------------------------------------------------------------
    // 2. Profissional envia mensagem com sucesso
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve permitir que o profissional envie mensagem no contrato ACTIVE")
    void devePermitirProfissionalEnviarMensagem() {

        Contract contract = buildContract(ContractStatus.ACTIVE);
        User professional = contract.getProfessional();

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.of(professional));

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message result = chatService.sendMessage(
                CONTRACT_ID,
                PROFESSIONAL_ID,
                "Olá, atleta!",
                null
        );

        assertNotNull(result);
        assertSame(professional, result.getSender());
        assertSame(contract.getAthlete(), result.getReceiver());
        assertEquals("Olá, atleta!", result.getContent());

        verify(messageRepository).save(any(Message.class));
    }

    // ---------------------------------------------------------------
    // 3. Contrato inexistente
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Não deve enviar mensagem quando o contrato não existe")
    void naoDeveEnviarMensagemContratoInexistente() {

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chatService.sendMessage(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        "Mensagem",
                        null
                )
        );

        assertEquals(
                "Contrato não encontrado",
                exception.getMessage()
        );

        verify(userRepository, never()).findById(any());
        verify(messageRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 4. Contrato não ACTIVE
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Não deve enviar mensagem em contrato que não está ACTIVE")
    void naoDeveEnviarMensagemContratoNaoAtivo() {

        Contract contract = buildContract(ContractStatus.PENDING);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> chatService.sendMessage(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        "Mensagem",
                        null
                )
        );

        assertEquals(
                "Chat disponível apenas em contratos ACTIVE",
                exception.getMessage()
        );

        verify(userRepository, never()).findById(any());
        verify(messageRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 5. Remetente inexistente
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Não deve enviar mensagem quando o remetente não existe")
    void naoDeveEnviarMensagemRemetenteInexistente() {

        Contract contract = buildContract(ContractStatus.ACTIVE);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(userRepository.findById(INTRUDER_ID))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chatService.sendMessage(
                        CONTRACT_ID,
                        INTRUDER_ID,
                        "Mensagem",
                        null
                )
        );

        assertEquals(
                "Remetente não encontrado",
                exception.getMessage()
        );

        verify(messageRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 6. Remetente não participa do contrato
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Não deve permitir mensagem de usuário que não participa do contrato")
    void naoDevePermitirUsuarioForaDoContratoEnviarMensagem() {

        Contract contract = buildContract(ContractStatus.ACTIVE);

        User intruder = new User();
        intruder.setId(INTRUDER_ID);
        intruder.setRole(Role.ATHLETE);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(userRepository.findById(INTRUDER_ID))
                .thenReturn(Optional.of(intruder));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> chatService.sendMessage(
                        CONTRACT_ID,
                        INTRUDER_ID,
                        "Mensagem indevida",
                        null
                )
        );

        assertEquals(
                "Você não tem permissão para enviar mensagem neste contrato",
                exception.getMessage()
        );

        verify(messageRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // 7. Preservar conteúdo e anexo
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve salvar conteúdo e anexo da mensagem corretamente")
    void deveSalvarConteudoEAnexoCorretamente() {

        Contract contract = buildContract(ContractStatus.ACTIVE);
        User athlete = contract.getAthlete();

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(athlete));

        ArgumentCaptor<Message> captor =
                ArgumentCaptor.forClass(Message.class);

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        chatService.sendMessage(
                CONTRACT_ID,
                ATHLETE_ID,
                "Confira meu progresso",
                "https://example.com/progresso.jpg"
        );

        verify(messageRepository).save(captor.capture());

        Message savedMessage = captor.getValue();

        assertEquals(
                "Confira meu progresso",
                savedMessage.getContent()
        );

        assertEquals(
                "https://example.com/progresso.jpg",
                savedMessage.getAttachmentUrl()
        );

        assertSame(
                contract.getProfessional(),
                savedMessage.getReceiver()
        );
    }

    // ===============================================================
    // GET MESSAGES
    // ===============================================================

    // ---------------------------------------------------------------
    // 8. Atleta consulta mensagens
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve permitir que o atleta consulte as mensagens do contrato")
    void devePermitirAtletaConsultarMensagens() {

        Contract contract = buildContract(ContractStatus.ACTIVE);

        Message message = Message.builder()
                .contract(contract)
                .sender(contract.getAthlete())
                .receiver(contract.getProfessional())
                .content("Olá!")
                .build();

        List<Message> messages = List.of(message);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(messageRepository.findByContractIdOrderByCreatedAtAsc(CONTRACT_ID))
                .thenReturn(messages);

        List<Message> result =
                chatService.getMessagesByContract(
                        CONTRACT_ID,
                        ATHLETE_ID
                );

        assertEquals(1, result.size());
        assertSame(message, result.get(0));

        verify(messageRepository)
                .findByContractIdOrderByCreatedAtAsc(CONTRACT_ID);
    }

    // ---------------------------------------------------------------
    // 9. Profissional consulta mensagens
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve permitir que o profissional consulte as mensagens do contrato")
    void devePermitirProfissionalConsultarMensagens() {

        Contract contract = buildContract(ContractStatus.ACTIVE);

        Message message = Message.builder()
                .contract(contract)
                .sender(contract.getProfessional())
                .receiver(contract.getAthlete())
                .content("Vamos começar o treino.")
                .build();

        List<Message> messages = Arrays.asList(message);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(messageRepository.findByContractIdOrderByCreatedAtAsc(CONTRACT_ID))
                .thenReturn(messages);

        List<Message> result =
                chatService.getMessagesByContract(
                        CONTRACT_ID,
                        PROFESSIONAL_ID
                );

        assertEquals(1, result.size());
        assertSame(message, result.get(0));

        verify(messageRepository)
                .findByContractIdOrderByCreatedAtAsc(CONTRACT_ID);
    }

    // ---------------------------------------------------------------
    // 10. Contrato inexistente ao consultar
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Não deve consultar mensagens quando o contrato não existe")
    void naoDeveConsultarMensagensContratoInexistente() {

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chatService.getMessagesByContract(
                        CONTRACT_ID,
                        ATHLETE_ID
                )
        );

        assertEquals(
                "Contrato não encontrado",
                exception.getMessage()
        );

        verify(messageRepository, never())
                .findByContractIdOrderByCreatedAtAsc(any());
    }

    // ---------------------------------------------------------------
    // 11. Usuário fora do contrato
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Não deve permitir que usuário fora do contrato veja o chat")
    void naoDevePermitirNaoParticipanteConsultarMensagens() {

        Contract contract = buildContract(ContractStatus.ACTIVE);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> chatService.getMessagesByContract(
                        CONTRACT_ID,
                        INTRUDER_ID
                )
        );

        assertEquals(
                "Você não tem acesso a este chat",
                exception.getMessage()
        );

        verify(messageRepository, never())
                .findByContractIdOrderByCreatedAtAsc(any());
    }
}