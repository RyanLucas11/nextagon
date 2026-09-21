package br.com.nextagon.service;

import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.ProfessionalProfileRepository;
import br.com.nextagon.repository.ReviewRepository;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ProfessionalProfileRepository professionalProfileRepository;

    @InjectMocks
    private ReviewService reviewService;

    private static final String CONTRACT_ID = "contract-1";
    private static final String ATHLETE_ID = "athlete-1";
    private static final String PROFESSIONAL_ID = "prof-1";
    private static final String OUTRO_ATHLETE_ID = "athlete-2";

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

    @Test
    @DisplayName("Deve criar uma avaliação com sucesso para contrato COMPLETED")
    void deveCriarReviewComSucesso() {
        Contract contratoConcluido = buildContract(ContractStatus.COMPLETED);
        ProfessionalProfile perfil = new ProfessionalProfile();
        perfil.setUser(buildProfessional(PROFESSIONAL_ID));

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contratoConcluido));
        when(reviewRepository.existsByContractId(CONTRACT_ID))
                .thenReturn(false);
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(professionalProfileRepository.findByUserId(PROFESSIONAL_ID))
                .thenReturn(Optional.of(perfil));
        when(reviewRepository.calculateAverageRatingByProfessional(PROFESSIONAL_ID))
                .thenReturn(4.5);
        when(reviewRepository.countByProfessionalId(PROFESSIONAL_ID))
                .thenReturn(3L);

        Review resultado = reviewService.createReview(CONTRACT_ID, ATHLETE_ID, 5, "Ótimo profissional");

        assertEquals(5, resultado.getRating());
        assertEquals("Ótimo profissional", resultado.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(professionalProfileRepository, times(1)).save(any(ProfessionalProfile.class));
    }

    @Test
    @DisplayName("Não deve permitir criar review com rating fora do intervalo 1-5")
    void naoDevePermitirRatingForaDoIntervalo() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(CONTRACT_ID, ATHLETE_ID, 6, "Nota inválida")
        );

        assertEquals("Rating deve ser entre 1 e 5", exception.getMessage());
        verify(contractRepository, never()).findById(any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o contrato não existe")
    void deveLancarExcecaoQuandoContratoNaoExiste() {
        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(CONTRACT_ID, ATHLETE_ID, 5, "Comentário")
        );

        assertEquals("Contrato não encontrado", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve permitir avaliar um contrato que não está COMPLETED")
    void naoDevePermitirAvaliarContratoNaoConcluido() {
        Contract contratoAtivo = buildContract(ContractStatus.ACTIVE);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contratoAtivo));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(CONTRACT_ID, ATHLETE_ID, 5, "Comentário")
        );

        assertEquals("Só é possível avaliar contratos com status COMPLETED", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve permitir que outro usuário avalie um contrato que não é seu")
    void naoDevePermitirReviewerQueNaoEhOAtleta() {
        Contract contratoConcluido = buildContract(ContractStatus.COMPLETED);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contratoConcluido));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> reviewService.createReview(CONTRACT_ID, OUTRO_ATHLETE_ID, 5, "Comentário")
        );

        assertEquals("Apenas o atleta do contrato pode avaliá-lo", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve permitir avaliar o mesmo contrato duas vezes")
    void naoDevePermitirReviewDuplicada() {
        Contract contratoConcluido = buildContract(ContractStatus.COMPLETED);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contratoConcluido));
        when(reviewRepository.existsByContractId(CONTRACT_ID))
                .thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(CONTRACT_ID, ATHLETE_ID, 5, "Comentário")
        );

        assertEquals("Este contrato já foi avaliado", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve salvar a review com os dados corretos e recalcular a média do profissional")
    void deveSalvarReviewComDadosCorretosERecalcularMedia() {
        Contract contratoConcluido = buildContract(ContractStatus.COMPLETED);
        ProfessionalProfile perfil = new ProfessionalProfile();
        perfil.setUser(buildProfessional(PROFESSIONAL_ID));

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        ArgumentCaptor<ProfessionalProfile> perfilCaptor = ArgumentCaptor.forClass(ProfessionalProfile.class);

        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contratoConcluido));
        when(reviewRepository.existsByContractId(CONTRACT_ID))
                .thenReturn(false);
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(professionalProfileRepository.findByUserId(PROFESSIONAL_ID))
                .thenReturn(Optional.of(perfil));
        when(reviewRepository.calculateAverageRatingByProfessional(PROFESSIONAL_ID))
                .thenReturn(4.333);
        when(reviewRepository.countByProfessionalId(PROFESSIONAL_ID))
                .thenReturn(3L);

        reviewService.createReview(CONTRACT_ID, ATHLETE_ID, 4, "Muito bom");

        verify(reviewRepository).save(reviewCaptor.capture());
        Review reviewSalva = reviewCaptor.getValue();
        assertEquals(4, reviewSalva.getRating());
        assertEquals(ATHLETE_ID, reviewSalva.getReviewer().getId());
        assertEquals(PROFESSIONAL_ID, reviewSalva.getProfessional().getId());

        verify(professionalProfileRepository).save(perfilCaptor.capture());
        ProfessionalProfile perfilSalvo = perfilCaptor.getValue();
        assertEquals(4.33, perfilSalvo.getAverageRating());
        assertEquals(3, perfilSalvo.getTotalReviews());
    }
}