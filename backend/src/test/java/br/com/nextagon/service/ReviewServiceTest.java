
package br.com.nextagon.service;

import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.ProfessionalProfileRepository;
import br.com.nextagon.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private static final String CONTRACT_ID = "contract-1";
    private static final String ATHLETE_ID = "athlete-1";
    private static final String PROFESSIONAL_ID = "professional-1";

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ProfessionalProfileRepository professionalProfileRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User athlete;
    private User professional;
    private Contract contract;

    @BeforeEach
    void setUp() {
        athlete = new User();
        athlete.setId(ATHLETE_ID);
        athlete.setName("Atleta Teste");
        athlete.setRole(Role.ATHLETE);
        athlete.setActive(true);

        professional = new User();
        professional.setId(PROFESSIONAL_ID);
        professional.setName("Profissional Teste");
        professional.setRole(Role.PROFESSIONAL);
        professional.setActive(true);

        contract = new Contract();
        contract.setId(CONTRACT_ID);
        contract.setAthlete(athlete);
        contract.setProfessional(professional);
        contract.setStatus(ContractStatus.COMPLETED);
    }

    private void mockContract() {
        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract));
    }

    private ProfessionalProfile mockLockedProfile() {
        ProfessionalProfile profile = new ProfessionalProfile();

        when(professionalProfileRepository.findByUserIdForUpdate(
                PROFESSIONAL_ID
        )).thenReturn(Optional.of(profile));

        return profile;
    }

    private void mockSavedReview() {
        when(reviewRepository.saveAndFlush(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void assertStatus(
            HttpStatus expected,
            ResponseStatusException exception
    ) {
        assertEquals(expected, exception.getStatusCode());
    }

    @Test
    @DisplayName("Deve criar avaliação válida")
    void deveCriarAvaliacaoValida() {
        mockContract();
        mockLockedProfile();
        mockSavedReview();

        Review result = reviewService.createReview(
                CONTRACT_ID,
                ATHLETE_ID,
                5,
                "Excelente profissional"
        );

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Excelente profissional", result.getComment());
        assertSame(athlete, result.getReviewer());
        assertSame(professional, result.getProfessional());
        assertSame(contract, result.getContract());

        verify(reviewRepository).saveAndFlush(any(Review.class));
    }

    @Test
    @DisplayName("Deve rejeitar nota inválida")
    void deveRejeitarNotaInvalida() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        6,
                        "Teste"
                )
        );

        assertStatus(HttpStatus.BAD_REQUEST, exception);
        verifyNoInteractions(contractRepository);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("Deve rejeitar contrato inexistente")
    void deveRejeitarContratoInexistente() {
        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        5,
                        "Teste"
                )
        );

        assertStatus(HttpStatus.NOT_FOUND, exception);

        verify(reviewRepository, never())
                .saveAndFlush(any(Review.class));
    }

    @Test
    @DisplayName("Somente o atleta do contrato pode avaliar")
    void deveBloquearAvaliacaoDeTerceiros() {
        mockContract();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        CONTRACT_ID,
                        "outro-usuario",
                        5,
                        "Teste"
                )
        );

        assertStatus(HttpStatus.FORBIDDEN, exception);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("Atleta inativo não pode avaliar")
    void deveBloquearAtletaInativo() {
        athlete.setActive(false);
        mockContract();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        5,
                        "Teste"
                )
        );

        assertStatus(HttpStatus.FORBIDDEN, exception);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("Profissional inativo não pode receber avaliação")
    void deveBloquearProfissionalInativo() {
        professional.setActive(false);
        mockContract();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        5,
                        "Teste"
                )
        );

        assertStatus(HttpStatus.CONFLICT, exception);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("Contrato não concluído não pode ser avaliado")
    void deveRejeitarContratoNaoConcluido() {
        contract.setStatus(ContractStatus.ACTIVE);
        mockContract();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        5,
                        "Teste"
                )
        );

        assertStatus(HttpStatus.CONFLICT, exception);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("Contrato não pode receber duas avaliações")
    void deveBloquearAvaliacaoDuplicada() {
        mockContract();
        mockLockedProfile();

        when(reviewRepository.existsByContractId(CONTRACT_ID))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        5,
                        "Teste"
                )
        );

        assertStatus(HttpStatus.CONFLICT, exception);

        verify(reviewRepository, never())
                .saveAndFlush(any(Review.class));
    }

    @Test
    @DisplayName("Deve atualizar média e quantidade de avaliações")
    void deveAtualizarMediaDoProfissional() {
        mockContract();

        ProfessionalProfile profile = mockLockedProfile();
        mockSavedReview();

        when(reviewRepository.calculateAverageRatingByProfessional(
                PROFESSIONAL_ID
        )).thenReturn(4.375);

        when(reviewRepository.countByProfessionalId(
                PROFESSIONAL_ID
        )).thenReturn(8L);

        reviewService.createReview(
                CONTRACT_ID,
                ATHLETE_ID,
                5,
                "Ótimo atendimento"
        );

        assertEquals(4.38, profile.getAverageRating(), 0.001);
        assertEquals(8, profile.getTotalReviews());

        verify(professionalProfileRepository).save(profile);
    }

    @Test
    @DisplayName("Deve aceitar comentário opcional")
    void deveAceitarComentarioNulo() {
        mockContract();
        mockLockedProfile();
        mockSavedReview();

        Review result = reviewService.createReview(
                CONTRACT_ID,
                ATHLETE_ID,
                4,
                null
        );

        assertNull(result.getComment());
        assertEquals(4, result.getRating());
    }

    @Test
    @DisplayName("Deve rejeitar comentário muito longo")
    void deveRejeitarComentarioMuitoLongo() {
        String comment = "A".repeat(2001);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        5,
                        comment
                )
        );

        assertStatus(HttpStatus.BAD_REQUEST, exception);
        verifyNoInteractions(contractRepository);
    }

    @Test
    @DisplayName("Deve listar avaliações do profissional")
    void deveListarAvaliacoesDoProfissional() {
        Review review = Review.builder()
                .contract(contract)
                .reviewer(athlete)
                .professional(professional)
                .rating(5)
                .build();

        when(reviewRepository.findByProfessionalId(PROFESSIONAL_ID))
                .thenReturn(List.of(review));

        List<Review> result = reviewService
                .getReviewsByProfessional(PROFESSIONAL_ID);

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getRating());
    }

    @Test
    @DisplayName("Não deve avaliar profissional sem perfil")
    void deveBloquearProfissionalSemPerfil() {
        mockContract();

        when(professionalProfileRepository.findByUserIdForUpdate(
                PROFESSIONAL_ID
        )).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        CONTRACT_ID,
                        ATHLETE_ID,
                        5,
                        "Excelente"
                )
        );

        assertStatus(HttpStatus.CONFLICT, exception);

        verify(reviewRepository, never())
                .saveAndFlush(any(Review.class));
    }
}
