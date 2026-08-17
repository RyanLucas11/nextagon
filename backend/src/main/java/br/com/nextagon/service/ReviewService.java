package br.com.nextagon.service;

import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.ProfessionalProfileRepository;
import br.com.nextagon.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ContractRepository contractRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;

    @Transactional
    public Review createReview(String contractId, String reviewerId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating deve ser entre 1 e 5");
        }

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (contract.getStatus() != ContractStatus.COMPLETED) {
            throw new IllegalStateException("Só é possível avaliar contratos com status COMPLETED");
        }

        if (!contract.getAthlete().getId().equals(reviewerId)) {
            throw new SecurityException("Apenas o atleta do contrato pode avaliá-lo");
        }

        if (reviewRepository.existsByContractId(contractId)) {
            throw new IllegalStateException("Este contrato já foi avaliado");
        }

        Review review = Review.builder()
                .contract(contract)
                .reviewer(contract.getAthlete())
                .professional(contract.getProfessional())
                .rating(rating)
                .comment(comment)
                .build();

        reviewRepository.save(review);

        // Atualiza média real usando query de agregação no banco
        recalculateProfessionalRating(contract.getProfessional().getId());

        return review;
    }

    public List<Review> getReviewsByProfessional(String professionalId) {
        return reviewRepository.findByProfessionalId(professionalId);
    }

    private void recalculateProfessionalRating(String professionalUserId) {
        ProfessionalProfile profile = professionalProfileRepository
                .findByUserId(professionalUserId)
                .orElse(null);

        if (profile == null) return;

        Double avg = reviewRepository.calculateAverageRatingByProfessional(professionalUserId);
        Long count = reviewRepository.countByProfessionalId(professionalUserId);

        profile.setAverageRating(avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0);
        profile.setTotalReviews(count != null ? count.intValue() : 0);
        professionalProfileRepository.save(profile);
    }
}