
package br.com.nextagon.service;

import br.com.nextagon.model.Contract;
import br.com.nextagon.model.ContractStatus;
import br.com.nextagon.model.Role;
import br.com.nextagon.model.User;
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
class ContractServiceTest {

    private static final String ATHLETE_ID = "athlete-1";
    private static final String PROFESSIONAL_ID = "professional-1";
    private static final String OTHER_USER_ID = "other-user";
    private static final String CONTRACT_ID = "contract-1";

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ContractService contractService;

    private User buildUser(String id, Role role, boolean active) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setActive(active);
        return user;
    }

    private Contract buildContract(ContractStatus status) {
        Contract contract = new Contract();
        contract.setId(CONTRACT_ID);
        contract.setAthlete(
                buildUser(ATHLETE_ID, Role.ATHLETE, true)
        );
        contract.setProfessional(
                buildUser(PROFESSIONAL_ID, Role.PROFESSIONAL, true)
        );
        contract.setStatus(status);
        return contract;
    }

    private void assertHttpStatus(
            HttpStatus expected,
            ResponseStatusException exception
    ) {
        assertEquals(expected, exception.getStatusCode());
    }

    @Test
    @DisplayName("Atleta ativo pode solicitar contrato")
    void deveCriarContratoParaAtletaAtivo() {
        User athlete = buildUser(
                ATHLETE_ID, Role.ATHLETE, true
        );

        User professional = buildUser(
                PROFESSIONAL_ID, Role.PROFESSIONAL, true
        );

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(athlete));

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.of(professional));

        when(contractRepository
                .findByAthleteIdAndProfessionalIdAndStatus(
                        ATHLETE_ID,
                        PROFESSIONAL_ID,
                        ContractStatus.ACTIVE
                ))
                .thenReturn(Optional.empty());

        when(contractRepository.save(any(Contract.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Contract result = contractService.createContractRequest(
                ATHLETE_ID,
                PROFESSIONAL_ID,
                "Gostaria de contratar seus serviços"
        );

        assertNotNull(result);
        assertEquals(ContractStatus.PENDING, result.getStatus());
        assertEquals(ATHLETE_ID, result.getAthlete().getId());
        assertEquals(
                PROFESSIONAL_ID,
                result.getProfessional().getId()
        );

        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    @DisplayName("Profissional não pode solicitar contrato como atleta")
    void naoDevePermitirProfissionalSolicitarContrato() {
        User invalidAthlete = buildUser(
                ATHLETE_ID, Role.PROFESSIONAL, true
        );

        User professional = buildUser(
                PROFESSIONAL_ID, Role.PROFESSIONAL, true
        );

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(invalidAthlete));

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.of(professional));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contractService.createContractRequest(
                        ATHLETE_ID,
                        PROFESSIONAL_ID,
                        "Solicitação"
                )
        );

        assertHttpStatus(HttpStatus.FORBIDDEN, exception);

        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve criar contrato quando já existe um ativo")
    void naoDeveCriarContratoAtivoDuplicado() {
        User athlete = buildUser(
                ATHLETE_ID, Role.ATHLETE, true
        );

        User professional = buildUser(
                PROFESSIONAL_ID, Role.PROFESSIONAL, true
        );

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(athlete));

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.of(professional));

        when(contractRepository
                .findByAthleteIdAndProfessionalIdAndStatus(
                        ATHLETE_ID,
                        PROFESSIONAL_ID,
                        ContractStatus.ACTIVE
                ))
                .thenReturn(Optional.of(
                        buildContract(ContractStatus.ACTIVE)
                ));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contractService.createContractRequest(
                        ATHLETE_ID,
                        PROFESSIONAL_ID,
                        "Solicitação"
                )
        );

        assertHttpStatus(HttpStatus.CONFLICT, exception);

        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("Profissional responsável pode aceitar contrato")
    void devePermitirProfissionalAceitarContrato() {
        User professional = buildUser(
                PROFESSIONAL_ID, Role.PROFESSIONAL, true
        );

        Contract contract = buildContract(ContractStatus.PENDING);

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.of(professional));

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(contractRepository.save(any(Contract.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Contract result = contractService.respondToContract(
                CONTRACT_ID,
                PROFESSIONAL_ID,
                true
        );

        assertEquals(ContractStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getStartedAt());

        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Outro profissional não pode aceitar contrato")
    void naoDevePermitirOutroProfissionalAceitarContrato() {
        User otherProfessional = buildUser(
                OTHER_USER_ID, Role.PROFESSIONAL, true
        );

        Contract contract = buildContract(ContractStatus.PENDING);

        when(userRepository.findById(OTHER_USER_ID))
                .thenReturn(Optional.of(otherProfessional));

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contractService.respondToContract(
                        CONTRACT_ID,
                        OTHER_USER_ID,
                        true
                )
        );

        assertHttpStatus(HttpStatus.FORBIDDEN, exception);
        assertEquals(ContractStatus.PENDING, contract.getStatus());

        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("Profissional responsável pode concluir contrato ativo")
    void devePermitirConcluirContratoAtivo() {
        User professional = buildUser(
                PROFESSIONAL_ID, Role.PROFESSIONAL, true
        );

        Contract contract = buildContract(ContractStatus.ACTIVE);

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.of(professional));

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        when(contractRepository.save(any(Contract.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Contract result = contractService.completeContract(
                CONTRACT_ID,
                PROFESSIONAL_ID
        );

        assertEquals(ContractStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt());

        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Outro profissional não pode concluir contrato")
    void naoDevePermitirOutroProfissionalConcluirContrato() {
        User otherProfessional = buildUser(
                OTHER_USER_ID, Role.PROFESSIONAL, true
        );

        Contract contract = buildContract(ContractStatus.ACTIVE);

        when(userRepository.findById(OTHER_USER_ID))
                .thenReturn(Optional.of(otherProfessional));

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contractService.completeContract(
                        CONTRACT_ID,
                        OTHER_USER_ID
                )
        );

        assertHttpStatus(HttpStatus.FORBIDDEN, exception);
        assertEquals(ContractStatus.ACTIVE, contract.getStatus());

        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("Usuário com papel incorreto não pode listar contratos de atleta")
    void naoDevePermitirPapelIncorretoConsultarContratosAtleta() {
        User user = buildUser(
                ATHLETE_ID, Role.PROFESSIONAL, true
        );

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contractService.getContractsByAthlete(ATHLETE_ID)
        );

        assertHttpStatus(HttpStatus.FORBIDDEN, exception);

        verify(contractRepository, never())
                .findByAthleteId(any());
    }

    @Test
    @DisplayName("Profissional inativo não pode listar contratos")
    void naoDevePermitirProfissionalInativoConsultarContratos() {
        User professional = buildUser(
                PROFESSIONAL_ID, Role.PROFESSIONAL, false
        );

        when(userRepository.findById(PROFESSIONAL_ID))
                .thenReturn(Optional.of(professional));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contractService.getContractsByProfessional(
                        PROFESSIONAL_ID
                )
        );

        assertHttpStatus(HttpStatus.FORBIDDEN, exception);

        verify(contractRepository, never())
                .findByProfessionalId(any());
    }

    @Test
    @DisplayName("Atleta ativo pode consultar seus contratos")
    void devePermitirAtletaConsultarContratos() {
        User athlete = buildUser(
                ATHLETE_ID, Role.ATHLETE, true
        );

        Contract contract = buildContract(ContractStatus.ACTIVE);

        when(userRepository.findById(ATHLETE_ID))
                .thenReturn(Optional.of(athlete));

        when(contractRepository.findByAthleteId(ATHLETE_ID))
                .thenReturn(List.of(contract));

        List<Contract> result =
                contractService.getContractsByAthlete(ATHLETE_ID);

        assertEquals(1, result.size());
        assertEquals(CONTRACT_ID, result.get(0).getId());
    }

    @Test
    @DisplayName("Usuário que não participa não pode ler mensagens do contrato")
    void naoDevePermitirTerceiroConsultarMensagens() {
        Contract contract = buildContract(ContractStatus.ACTIVE);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contractService.getMessagesByContract(
                        CONTRACT_ID,
                        OTHER_USER_ID
                )
        );

        assertHttpStatus(HttpStatus.FORBIDDEN, exception);

        verifyNoInteractions(messageRepository);
    }
}
