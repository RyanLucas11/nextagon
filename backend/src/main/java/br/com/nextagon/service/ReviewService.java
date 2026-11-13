
package br.com.nextagon.service;

import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.ProfessionalProfileRepository;
import br.com.nextagon.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ContractRepository contractRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;

    @Transactional
    public Review createReview(
            String contractId,
            String reviewerId,
            int rating,
            String comment
    ) {
        if (contractId == null || contractId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID do contrato é obrigatório"
            );
        }

        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A nota deve estar entre 1 e 5"
            );
        }

        if (comment != null && comment.length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O comentário deve ter no máximo 2000 caracteres"
            );
        }

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Contrato não encontrado"
                ));

        if (!contract.getAthlete().getId().equals(reviewerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas o atleta do contrato pode avaliá-lo"
            );
        }

        if (!contract.getAthlete().isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "A conta do atleta está inativa"
            );
        }

        if (!contract.getProfessional().isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível avaliar um profissional com conta inativa"
            );
        }

        if (contract.getStatus() != ContractStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Apenas contratos concluídos podem ser avaliados"
            );
        }

        String professionalId = contract.getProfessional().getId();

        ProfessionalProfile profile = professionalProfileRepository
                .findByUserIdForUpdate(professionalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "O perfil profissional precisa ser regularizado antes de receber avaliações"
                ));

        if (reviewRepository.existsByContractId(contractId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este contrato já foi avaliado"
            );
        }

        Review review = Review.builder()
                .contract(contract)
                .reviewer(contract.getAthlete())
                .professional(contract.getProfessional())
                .rating(rating)
                .comment(
                        comment == null || comment.isBlank()
                                ? null
                                : comment.trim()
                )
                .build();

        Review savedReview = reviewRepository.saveAndFlush(review);

        recalculateProfessionalRating(professionalId, profile);

        return savedReview;
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsByProfessional(
            String professionalId
    ) {
        if (professionalId == null || professionalId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID do profissional é obrigatório"
            );
        }

        return reviewRepository.findByProfessionalId(professionalId);
    }

    private void recalculateProfessionalRating(
            String professionalId,
            ProfessionalProfile profile
    ) {
        Double average = reviewRepository
                .calculateAverageRatingByProfessional(professionalId);

        Long count = reviewRepository
                .countByProfessionalId(professionalId);

        double roundedAverage = average == null
                ? 0.0
                : Math.round(average * 100.0) / 100.0;

        profile.setAverageRating(roundedAverage);
        profile.setTotalReviews(
                count == null ? 0 : Math.toIntExact(count)
        );

        professionalProfileRepository.save(profile);
    }
}
